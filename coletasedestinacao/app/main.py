import os, json, hashlib, logging, threading
from contextlib import asynccontextmanager
from uuid import UUID
import boto3
from botocore.config import Config
from botocore.exceptions import BotoCoreError, ClientError
from fastapi import FastAPI, HTTPException, UploadFile, File, Query
from fastapi.responses import StreamingResponse
from pydantic import BaseModel, Field
from . import db, broker
from .domain import transicao

logging.basicConfig(level=logging.INFO)
bucket=os.getenv('S3_BUCKET','coleta-comprovantes')

def s3():
    return boto3.client('s3',endpoint_url=os.environ['S3_ENDPOINT'],region_name=os.getenv('AWS_REGION','us-east-1'),config=Config(s3={'addressing_style':'path'},connect_timeout=3,read_timeout=5,retries={'max_attempts':2}))

@asynccontextmanager
async def lifespan(app):
    db.iniciar(); broker.stop.clear()
    threads=[threading.Thread(target=f,daemon=True) for f in (broker.consumir,broker.publicar)]
    for t in threads: t.start()
    yield
    broker.stop.set()
    for t in threads: t.join(timeout=12)

app=FastAPI(title='Coletas e Destinação',version='1.0.0',lifespan=lifespan)

@app.get('/actuator/health')
@app.get('/health')
def health():
    try:
        with db.tx() as c: c.execute('SELECT 1 FROM dual'); c.fetchone()
        s3().head_bucket(Bucket=bucket)
        if not broker.consumer_ready.is_set(): raise RuntimeError('RabbitMQ indisponível')
    except Exception: raise HTTPException(503,'DOWN')
    return {'status':'UP'}

COLS='id,solicitacao_id,correlation_id,status,dados,criado_em,atualizado_em'
def representar(r):
    return dict(id=r[0],solicitacaoId=r[1],correlationId=r[2],status=r[3],dados=json.loads(db.texto(r[4])),criadoEm=r[5],atualizadoEm=r[6])

def buscar(c,id,lock=False):
    c.execute(f'SELECT {COLS} FROM CD_COLETA WHERE id=:1'+(' FOR UPDATE' if lock else ''),[str(id)])
    r=c.fetchone()
    if not r: raise HTTPException(404,'Coleta não encontrada')
    return representar(r)

@app.get('/api/coletas')
def listar(offset:int=Query(0,ge=0),limit:int=Query(20,ge=1,le=100)):
    with db.tx() as c:
        c.execute(f'SELECT {COLS} FROM CD_COLETA ORDER BY criado_em,id OFFSET :1 ROWS FETCH NEXT :2 ROWS ONLY',[offset,limit]); return [representar(r) for r in c.fetchall()]

@app.get('/api/coletas/{id}')
def consultar(id:UUID):
    with db.tx() as c: return buscar(c,id)

class Execucao(BaseModel):
    responsavel:str=Field(min_length=1,max_length=150)
    observacao:str=Field(default='',max_length=1000)
class Triagem(BaseModel):
    pesoKg:float=Field(gt=0,allow_inf_nan=False)
    classificacao:str=Field(min_length=1,max_length=150)
class Destinacao(BaseModel):
    destino:str=Field(min_length=1,max_length=200)
    metodo:str=Field(min_length=1,max_length=150)
    comprovanteId:str=Field(min_length=64,max_length=64)

def alterar(id,destino,dados):
    with db.tx() as c:
        coleta=buscar(c,id,True)
        if coleta['status']==destino and coleta['dados'].get(destino)==dados: return coleta
        try: tipo,rota=transicao(coleta['status'],destino)
        except ValueError as e: raise HTTPException(409,str(e))
        if destino=='DESTINADA':
            c.execute('SELECT 1 FROM CD_COMPROVANTE WHERE id=:1 AND coleta_id=:2',[dados['comprovanteId'],str(id)])
            if not c.fetchone(): raise HTTPException(422,'Comprovante não pertence à coleta')
        coleta['dados'][destino]=dados
        c.execute('UPDATE CD_COLETA SET status=:1,dados=:2,atualizado_em=SYS_EXTRACT_UTC(SYSTIMESTAMP) WHERE id=:3',[destino,json.dumps(coleta['dados']),str(id)])
        db.outbox(c,tipo,rota,coleta['correlationId'],dict(coletaId=str(id),solicitacaoId=coleta['solicitacaoId'],status=destino,**dados))
        return buscar(c,id)

@app.post('/api/coletas/{id}/execucao')
def executar(id:UUID,dados:Execucao): return alterar(id,'EXECUTADA',dados.model_dump())
@app.post('/api/coletas/{id}/triagem')
def triar(id:UUID,dados:Triagem): return alterar(id,'TRIADA',dados.model_dump())
@app.post('/api/coletas/{id}/destinacao')
def destinar(id:UUID,dados:Destinacao): return alterar(id,'DESTINADA',dados.model_dump())

@app.post('/api/coletas/{id}/comprovantes',status_code=201)
def anexar(id:UUID,arquivo:UploadFile=File(...)):
    conteudo=arquivo.file.read(10*1024*1024+1)
    if not conteudo or len(conteudo)>10*1024*1024: raise HTTPException(422,'Arquivo vazio ou maior que 10 MiB')
    mime=arquivo.content_type or ''
    assinaturas={'application/pdf':b'%PDF-','image/png':b'\x89PNG\r\n\x1a\n','image/jpeg':b'\xff\xd8\xff'}
    if mime not in assinaturas or not conteudo.startswith(assinaturas[mime]): raise HTTPException(415,'Use PDF, PNG ou JPEG válido')
    cid=hashlib.sha256(str(id).encode()+conteudo).hexdigest(); chave=f'coletas/{id}/{cid}'
    with db.tx() as c:
        buscar(c,id,True)
        c.execute('SELECT id FROM CD_COMPROVANTE WHERE id=:1',[cid])
        if not c.fetchone():
            try: s3().put_object(Bucket=bucket,Key=chave,Body=conteudo,ContentType=mime)
            except (BotoCoreError,ClientError): raise HTTPException(503,'S3 indisponível')
            c.execute('INSERT INTO CD_COMPROVANTE(id,coleta_id,objeto,nome,mime,tamanho) VALUES(:1,:2,:3,:4,:5,:6)',[cid,str(id),chave,(arquivo.filename or 'comprovante')[:255],mime,len(conteudo)])
    return {'id':cid,'coletaId':str(id),'tamanho':len(conteudo)}

@app.get('/api/coletas/{id}/comprovantes')
def comprovantes(id:UUID):
    with db.tx() as c:
        buscar(c,id); c.execute('SELECT id,nome,mime,tamanho FROM CD_COMPROVANTE WHERE coleta_id=:1',[str(id)])
        return [dict(id=r[0],nome=r[1],mime=r[2],tamanho=r[3]) for r in c.fetchall()]

@app.get('/api/coletas/{id}/comprovantes/{cid}')
def baixar(id:UUID,cid:str):
    with db.tx() as c:
        c.execute('SELECT objeto,mime FROM CD_COMPROVANTE WHERE coleta_id=:1 AND id=:2',[str(id),cid]); r=c.fetchone()
        if not r: raise HTTPException(404,'Comprovante não encontrado')
    try: body=s3().get_object(Bucket=bucket,Key=r[0])['Body']
    except (BotoCoreError,ClientError): raise HTTPException(503,'Comprovante indisponível no S3')
    def chunks():
        try: yield from body.iter_chunks(65536)
        finally: body.close()
    return StreamingResponse(chunks(),media_type=r[1],headers={'Content-Disposition':'attachment'})
