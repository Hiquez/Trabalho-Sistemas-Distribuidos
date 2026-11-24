from datetime import datetime, timezone
from uuid import uuid4

TRANSICOES = {'CONFIRMADA': ('EXECUTADA','ColetaExecutada','coleta.executada'), 'EXECUTADA': ('TRIADA','ColetaTriada','coleta.triada'), 'TRIADA': ('DESTINADA','ColetaDestinada','coleta.destinada')}

def evento(tipo, correlacao, payload):
    return dict(messageId=str(uuid4()), correlationId=correlacao, type=tipo, version=1, occurredAt=datetime.now(timezone.utc).isoformat(), payload=payload)

def transicao(atual, destino):
    if atual not in TRANSICOES or TRANSICOES[atual][0] != destino:
        raise ValueError(f'Transição inválida: {atual} -> {destino}')
    return TRANSICOES[atual][1:]
