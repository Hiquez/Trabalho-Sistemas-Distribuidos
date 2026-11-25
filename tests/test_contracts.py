import json
from pathlib import Path
from uuid import uuid4
import pytest
from jsonschema import Draft202012Validator, FormatChecker
ROOT=Path(__file__).resolve().parents[1]/'contracts/events/v1'
SCHEMAS=sorted(ROOT.glob('*.schema.json'))
def validator(name):
    return Draft202012Validator(json.loads((ROOT/(name+'.schema.json')).read_text()),format_checker=FormatChecker())
def event(typ,payload):
    return dict(messageId=str(uuid4()),correlationId=str(uuid4()),type=typ,version=1,occurredAt='2026-11-24T12:00:00Z',payload=payload)
@pytest.mark.parametrize('path',SCHEMAS,ids=lambda p:p.stem)
def test_schema(path):
    Draft202012Validator.check_schema(json.loads(path.read_text()))
def test_confirmada():
    e=event('ColetaConfirmada',{k:str(uuid4()) for k in ('solicitacaoId','reservaId','compromissoId')}); v=validator('coleta-confirmada'); v.validate(e)
    del e['payload']['reservaId']; assert not v.is_valid(e)
def test_resultado():
    e=event('ResultadoReserva',dict(solicitacaoId=str(uuid4()),reservada=False)); v=validator('resultado-reserva'); v.validate(e)
    e['payload']['reservada']=True; assert not v.is_valid(e)
    e['payload']['reservaId']=str(uuid4()); v.validate(e)
def test_cancelamento_antecipado():
    e=event('CompromissoCancelado',dict(solicitacaoId=str(uuid4()),compromissoId=None,motivoCancelamento='timeout',dataCancelamento='2026-11-24T12:00:00Z')); validator('compromisso-cancelado').validate(e)
def test_poison():
    e=event('ColetaConfirmada',dict(solicitacaoId='invalid')); assert not validator('coleta-confirmada').is_valid(e)
