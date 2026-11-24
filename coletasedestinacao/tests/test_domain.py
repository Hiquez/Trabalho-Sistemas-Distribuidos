import pytest
from app.domain import transicao, evento
from uuid import UUID

def test_fluxo():
    assert transicao('CONFIRMADA','EXECUTADA')[0]=='ColetaExecutada'
    assert transicao('EXECUTADA','TRIADA')[0]=='ColetaTriada'
    assert transicao('TRIADA','DESTINADA')[0]=='ColetaDestinada'

@pytest.mark.parametrize('origem,destino',[('CONFIRMADA','DESTINADA'),('DESTINADA','EXECUTADA'),('EXECUTADA','CONFIRMADA')])
def test_invalida(origem,destino):
    with pytest.raises(ValueError): transicao(origem,destino)

def test_envelope():
    e=evento('ColetaExecutada','correlacao',{'status':'EXECUTADA'})
    assert UUID(e['messageId']) and e['version']==1 and e['correlationId']=='correlacao'
