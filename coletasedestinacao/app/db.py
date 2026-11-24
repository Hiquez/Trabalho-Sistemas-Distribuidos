import os, json, time
from pathlib import Path
from contextlib import contextmanager
import oracledb
from .domain import evento

pool = None

def iniciar():
    global pool
    pool = oracledb.create_pool(user=os.environ['DB_USERNAME'], password=os.environ['DB_PASSWORD'], dsn=os.environ['DB_DSN'], min=1,max=8,increment=1)
    # DDL idempotente; tolera criação simultânea por outras réplicas.
    with tx() as c:
        for arquivo in sorted((Path(__file__).resolve().parents[1]/'migrations').glob('*.sql')):
            for sql in arquivo.read_text().split(';'):
                if sql.strip():
                    try: c.execute(sql.strip())
                    except oracledb.DatabaseError as e:
                        if e.args[0].code != 955: raise

@contextmanager
def tx():
    with pool.acquire() as conn:
        with conn.cursor() as c:
            try:
                yield c
                conn.commit()
            except BaseException:
                conn.rollback(); raise

def texto(v): return v.read() if hasattr(v,'read') else v

def outbox(c, tipo, rota, correlacao, payload):
    e=evento(tipo,correlacao,payload)
    c.execute('INSERT INTO CD_OUTBOX(message_id,correlation_id,tipo_evento,routing_key,payload) VALUES(:1,:2,:3,:4,:5)',[e['messageId'],correlacao,tipo,rota,json.dumps(e)])

def unico(e): return isinstance(e,oracledb.IntegrityError) and e.args[0].code == 1
