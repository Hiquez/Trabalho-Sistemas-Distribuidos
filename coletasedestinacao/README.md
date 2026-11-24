# Coletas e Destinação — Python

FastAPI, Oracle Thin (python-oracledb), RabbitMQ (Pika), S3 (boto3/MiniStack). A escolha de Python e Oracle substitui Node.js e PostgreSQL do PDF por solicitação do projeto atual. Não há JWT neste escopo acadêmico.

## Inicialização
Na raiz, mantenha seu `.env` original (não incluído no ZIP de entrega). Execute `docker compose up -d --build coletas`. O serviço usa o schema COLETAS_DESTINACAO existente e cria tabelas CD_* por scripts SQL idempotentes. Não apague volumes. Para banco novo, o script database/init/01-users.sh provisiona os usuários; as tabelas dos outros serviços dependem das próprias migrações, que não foram alteradas. O ZIP original não continha migrations da Cooperativa.

- Swagger: http://localhost:8084/docs
- Health: http://localhost:8084/actuator/health
- RabbitMQ: http://localhost:15672

## Contrato de integração
Solicitações ainda não possui produtor implementado no ZIP recebido. A convenção proposta é exchange direto `coleta.exchange`, routing key `coleta.confirmada`, fila independente `coletas.coleta-confirmada`. Alinhar com Agenda/Solicitações antes da integração. O payload mínimo contém solicitacaoId UUID; reservaId e compromissoId são opcionais; campos adicionais são preservados. Os contratos novos são JSON Schema reais em contracts/events/v1/*.schema.json. Os arquivos antigos não foram modificados.

Publique uma vez ou cinco vezes o mesmo exemplo (content type application/json, delivery mode 2):
```json
{"messageId":"11111111-1111-4111-8111-111111111120","correlationId":"22222222-2222-4222-8222-222222222230","type":"ColetaConfirmada","version":1,"occurredAt":"2026-11-23T23:30:00Z","payload":{"solicitacaoId":"33333333-3333-4333-8333-333333333340","reservaId":"44444444-4444-4444-8444-444444444451"}}
```

## Fluxo operacional
GET /api/coletas retorna o ID gerado. POST /api/coletas/{id}/execucao com `{"responsavel":"Operador"}`; POST /api/coletas/{id}/triagem com `{"pesoKg":12.5,"classificacao":"Eletrônicos"}`; POST /api/coletas/{id}/comprovantes com multipart campo `arquivo` (PDF/PNG/JPEG até 10 MiB); POST /api/coletas/{id}/destinacao com `{"destino":"Recicladora","metodo":"Reciclagem","comprovanteId":"ID retornado pelo upload"}`. GET /api/coletas/{id}/comprovantes lista e GET /api/coletas/{id}/comprovantes/{cid} baixa. Não há criação manual de coleta: somente ColetaConfirmada. Transições fora de ordem retornam 409. Repetição imediata da mesma operação não cria novo evento.

## Testes manuais
Antes das operações REST, crie fila temporária `teste.coletas.resposta` e bindings no exchange `coleta.exchange` para `coleta.executada`, `coleta.triada`, `coleta.destinada`. Sem destino, a Outbox permanece pendente intencionalmente. Consulte:
```sql
SELECT id,solicitacao_id,status FROM CD_COLETA;
SELECT * FROM CD_INBOX;
SELECT tipo_evento,status,publicado_em FROM CD_OUTBOX;
SELECT * FROM CD_COMPROVANTE;
```
- Duplicidade: envie cinco cópias do evento acima; deve haver uma coleta e uma inbox para os IDs.
- Poison: envie JSON inválido; após três tentativas, verificar `coletas.coleta-confirmada.dlq`.
- Broker: pare RabbitMQ por 30 s, registre uma transição REST válida, verifique Outbox pendente, restaure RabbitMQ e aguarde publicação automática.
- Escala: altere `ports` de coletas para `["8080"]` e execute `docker compose up -d --scale coletas=3 coletas`; conferir três consumidores. Repita teste de duplicidade. Para testar interrupção controlada, use pausa temporária depois de processar e antes do ACK, mate o container e confirme reentrega e contagens.
- S3: envie e baixe comprovante e compare conteúdo; desligue MiniStack e verifique erro 503.

A publicação é at-least-once; uma falha entre confirmação do broker e atualização Oracle pode causar reenvio com mesmo messageId. Consumidores de saída devem deduplicar. Upload S3 e Oracle não são uma transação distribuída: chave determinística permite repetir upload após falha, mas objetos órfãos podem permanecer. O serviço não publica evento de criação em resposta a ColetaConfirmada, pois o PDF não exige esse evento; eventos de saída são gerados nas transições operacionais.

## Validação realizada
Compilação sintática Python e cinco testes unitários de domínio aprovados. Docker, Oracle, RabbitMQ e MiniStack não foram executados neste ambiente; testes de integração acima ainda precisam ser realizados. A Saga global e concorrência de 100 solicitações para 10 vagas pertencem à integração dos quatro serviços.
