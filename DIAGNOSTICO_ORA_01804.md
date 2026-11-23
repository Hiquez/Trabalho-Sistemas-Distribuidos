# Diagnostico ORA-01804

O stack trace fornecido mostra falha em `OracleDatabaseMetaData.getColumns()` durante `hibernate.hbm2ddl.auto=update`.

## Alteracao feita

Nos quatro microservicos, `ddl-auto` passou a aceitar `HIBERNATE_DDL_AUTO`, com padrao `none`, para evitar a introspeccao de tabelas que acionou a falha. O Compose injeta essa configuracao nos quatro servicos. Nenhuma tabela ou dado existente e apagado.

## Limitacao importante

**Este ajuste e diagnostico, nao uma solucao comprovada para ORA-01804.** Sem DDL automatico, tabelas novas NAO sao criadas. Este projeto nao possui scripts de migracao SQL versionados detectados na inspecao; portanto, se as tabelas nao existirem, as operacoes da aplicacao falharao. O Flyway continua habilitado.

Se o ORA-01804 persistir, investigar arquivos de timezone do servidor Oracle, ORA_TZFILE e erros do banco. Nao apagar volumes.

Para voltar ao comportamento anterior, definir `HIBERNATE_DDL_AUTO=update` no ambiente do Compose.

Execucao: `docker compose up -d --build --force-recreate` e `docker compose logs --tail=120 cooperativa`.
