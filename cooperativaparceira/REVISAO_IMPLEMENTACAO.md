# Revisao da Cooperativa Parceira

## Escopo e contratos

Este modulo preserva Java 21, Spring Boot, Oracle e RabbitMQ do ZIP fornecido. O PDF original especifica Python e PostgreSQL, enquanto este modulo utiliza Java 21 e Oracle. A infraestrutura AWS emulada foi ajustada para MiniStack. As diferencas de linguagem e banco devem ser aprovadas com o professor se a exigencia for literal. Os eventos seguem o envelope `messageId`, `correlationId`, `type`, `version`, `occurredAt`, `payload`.

## Alteracoes realizadas

- Removida configuracao `spring.application` duplicada e parametrizadas taxas e latencias por variaveis de ambiente.
- Removida dependencia AMQP duplicada no POM.
- Validacao de envelopes antes de acessar `payload` nos consumidores; tipo e versao do cancelamento validados.
- Removida reinicializacao desnecessaria de lock dentro da transacao de criacao; consumidores inicializam a linha de lock antes de chamar o service transacional.
- Mensagens de cancelamento agora registram `correlationId`, `solicitacaoId` e tipo na tabela de idempotencia.
- Outbox reserva um evento por vez, com snapshot imutavel e token; nao mantem transacao aberta durante espera por confirmacao RabbitMQ.
- Em falha de publicacao, o agendador interrompe o lote para nao repetir imediatamente a mesma mensagem.
- Processador de compromissos respeita interrupcao da thread.
- Adicionados testes unitarios de validacao de envelopes.

## Garantias e limites

- O fluxo oferece entrega **pelo menos uma vez**: apos ACK do RabbitMQ e antes do COMMIT do status `PUBLICADO`, uma falha pode causar republicacao. Consumidores devem deduplicar por `messageId`.
- Tres replicas podem simular latencia para o mesmo compromisso, mas o lock por solicitacao e a verificacao do status dentro da transacao impedem multiplas transicoes finais.
- Retry + DLQ requerem filas com DLX; filas preexistentes com argumentos diferentes devem ser recriadas em ambiente de desenvolvimento.
- O timeout global da saga e a liberacao da vaga sao responsabilidades de Solicitações e Agenda; nao sao implementados neste modulo.
- **Nao foi possivel executar Maven, Docker ou testes de integracao neste ambiente**. Validar build e cenarios de falha antes de declarar a entrega final.

## Validacao recomendada

1. `cd cooperativaparceira && ./mvnw clean test` (requer Maven Wrapper funcional ou Maven instalado).
2. `docker compose build cooperativa` usando o nome real do servico no compose.
3. Testar mensagens duplicadas com mesmo `messageId` e confirmar um unico compromisso.
4. Testar cancelamento anterior ao aceite e aceite tardio.
5. Derrubar RabbitMQ por 30s e confirmar escoamento posterior da Outbox.
6. Testar mensagem invalida na DLQ e concorrencia com tres replicas.
7. Executar testes integrados da saga quando os outros microsservicos estiverem prontos.
