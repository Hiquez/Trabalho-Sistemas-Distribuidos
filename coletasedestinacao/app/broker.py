import os, json, logging, threading, time
from pathlib import Path
from uuid import uuid4, UUID
import pika
from jsonschema import Draft202012Validator, FormatChecker
from . import db

log=logging.getLogger(__name__)
EXCHANGE='coleta.exchange'
QUEUE=os.getenv('MQ_QUEUE_COLETAS_COLETA_CONFIRMADA','coletas.coleta-confirmada')
ROTA='coleta.confirmada'
DLX='coletas.dlx'
DLQ=QUEUE+'.dlq'
stop=threading.Event()
consumer_ready=threading.Event()
schema_dir=Path(__file__).resolve().parents[1]/'contracts'
if not schema_dir.exists(): schema_dir=Path(__file__).resolve().parents[2]/'contracts'/'events'/'v1'
SCHEMA=json.loads((schema_dir/'coleta-confirmada.schema.json').read_text())
validator=Draft202012Validator(SCHEMA,format_checker=FormatChecker())

def conectar():
    return pika.BlockingConnection(pika.ConnectionParameters(host=os.getenv('SPRING_RABBITMQ_HOST','rabbitmq'),port=int(os.getenv('SPRING_RABBITMQ_PORT','5672')),credentials=pika.PlainCredentials(os.environ['SPRING_RABBITMQ_USERNAME'],os.environ['SPRING_RABBITMQ_PASSWORD']),heartbeat=60,blocked_connection_timeout=10,socket_timeout=5))

def topologia(ch):
    ch.exchange_declare(EXCHANGE,'direct',durable=True)
    ch.exchange_declare(DLX,'direct',durable=True)
    ch.queue_declare(DLQ,durable=True)
    ch.queue_bind(DLQ,DLX,DLQ)
    ch.queue_declare(QUEUE,durable=True,arguments={'x-dead-letter-exchange':DLX,'x-dead-letter-routing-key':DLQ})
    ch.queue_bind(QUEUE,EXCHANGE,ROTA)

def processar(body):
    e=json.loads(body); validator.validate(e)
    mid=str(UUID(e['messageId'])); sid=str(UUID(e['payload']['solicitacaoId'])); corr=str(UUID(e['correlationId']))
    with db.tx() as c:
        try: c.execute('INSERT INTO CD_INBOX(message_id) VALUES(:1)',[mid])
        except Exception as exc:
            if db.unico(exc): return
            raise
        try: c.execute('INSERT INTO CD_COLETA(id,solicitacao_id,correlation_id,dados) VALUES(:1,:2,:3,:4)',[str(uuid4()),sid,corr,json.dumps(e['payload'])])
        except Exception as exc:
            if not db.unico(exc): raise
    log.info('Coleta confirmada processada messageId=%s',mid)

def receber(ch,method,props,body):
    for tentativa in range(1,4):
        try:
            processar(body)
        except Exception:
            log.exception('Falha messageId=%s tentativa=%s/3',props.message_id,tentativa)
            if tentativa<3: time.sleep(tentativa)
        else:
            ch.basic_ack(method.delivery_tag); return
    ch.basic_nack(method.delivery_tag,requeue=False)

def consumir():
    while not stop.is_set():
        conn=None
        try:
            conn=conectar(); ch=conn.channel(); topologia(ch); ch.basic_qos(prefetch_count=1)
            ch.basic_consume(QUEUE,receber); consumer_ready.set()
            while not stop.is_set(): conn.process_data_events(time_limit=1)
        except Exception: log.exception('Consumidor desconectado')
        finally:
            consumer_ready.clear()
            if conn and conn.is_open:
                try: conn.close()
                except Exception: pass
        stop.wait(3)

def reservar():
    token=str(uuid4())
    with db.tx() as c:
        c.execute("SELECT message_id FROM CD_OUTBOX WHERE status='PENDENTE' OR (status='PROCESSANDO' AND reservado_ate < SYS_EXTRACT_UTC(SYSTIMESTAMP)) ORDER BY criado_em FETCH FIRST 20 ROWS ONLY")
        ids=[r[0] for r in c.fetchall()]
        for mid in ids:
            c.execute("UPDATE CD_OUTBOX SET status='PROCESSANDO', token_reserva=:1, reservado_ate=SYS_EXTRACT_UTC(SYSTIMESTAMP)+INTERVAL '60' SECOND WHERE message_id=:2 AND (status='PENDENTE' OR (status='PROCESSANDO' AND reservado_ate < SYS_EXTRACT_UTC(SYSTIMESTAMP)))",[token,mid])
            if c.rowcount:
                c.execute('SELECT routing_key,payload FROM CD_OUTBOX WHERE message_id=:1',[mid]); rota,payload=c.fetchone(); return mid,token,rota,db.texto(payload)

def finalizar(mid,token,ok):
    with db.tx() as c:
        c.execute("UPDATE CD_OUTBOX SET status=:1, publicado_em=CASE WHEN :2=1 THEN SYS_EXTRACT_UTC(SYSTIMESTAMP) ELSE NULL END, token_reserva=NULL,reservado_ate=NULL WHERE message_id=:3 AND token_reserva=:4",['PUBLICADO' if ok else 'PENDENTE',int(ok),mid,token])

def publicar():
    while not stop.is_set():
        conn=None
        try:
            conn=conectar(); ch=conn.channel(); topologia(ch); ch.confirm_delivery()
            while not stop.is_set():
                conn.process_data_events(time_limit=0)
                item=reservar()
                if not item: stop.wait(1); continue
                mid,token,rota,payload=item
                try: ch.basic_publish(EXCHANGE,rota,payload.encode(),pika.BasicProperties(content_type='application/json',delivery_mode=2,message_id=mid,correlation_id=json.loads(payload)['correlationId']),mandatory=True)
                except Exception:
                    finalizar(mid,token,False); raise
                finalizar(mid,token,True)
        except Exception: log.exception('Outbox aguardando nova tentativa')
        finally:
            if conn and conn.is_open:
                try: conn.close()
                except Exception: pass
        stop.wait(3)
