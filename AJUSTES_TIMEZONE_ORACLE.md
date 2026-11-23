# Ajustes de timezone Oracle / Java (todos os microservicos)

## O que foi alterado
- `compose.yaml`: `TZ=UTC` no Oracle e em todos os quatro servicos Java.
- Nos quatro servicos Java: `ORA_SDTZ=UTC` e `JAVA_TOOL_OPTIONS=-Duser.timezone=UTC -Doracle.jdbc.timezoneAsRegion=false`.
- `application.yml` dos quatro servicos: `spring.jpa.properties.hibernate.jdbc.time_zone=UTC`.
- URL JDBC de fallback para execucao local pela IDE: `localhost:1522/FREEPDB1`, correspondente ao mapeamento `1522:1521` do Compose.
- `spring.jackson.time-zone` foi preservado: controla JSON e nao a inicializacao do Oracle.
- Oracle, RabbitMQ, MiniStack e configuracoes dos contratos foram preservados.

## Como aplicar (PowerShell, na pasta do compose.yaml)
```powershell
docker compose config --quiet
docker compose up -d --build --force-recreate oracle solicitacoes agenda cooperativa coletas
docker compose ps -a
docker compose logs --tail=120 cooperativa
```

**Nao use `docker compose down -v`**: isso apaga volumes persistentes.

## Se ORA-01804 continuar
Essas alteracoes padronizam o timezone do cliente e do container, mas NAO garantem a correcao de um problema nos arquivos de timezone do banco Oracle.
Verifique primeiro:
```powershell
docker compose logs --tail=150 oracle
docker compose exec oracle date
docker inspect coleta-cooperativa --format '{{range .Config.Env}}{{println .}}{{end}}' | Select-String 'TZ|ORA_SDTZ|JAVA_TOOL_OPTIONS|DB_URL'
```
Se o Oracle estiver healthy, verifique no banco (SQL Developer) as consultas:
```sql
SELECT DBTIMEZONE, SESSIONTIMEZONE FROM DUAL;
SELECT filename, version FROM V$TIMEZONE_FILE;
```
O segundo SELECT pode exigir privilegios de catalogo. Nao remova arquivos de timezone, volumes nem recrie o banco sem diagnostico.

## Limites da validacao
Foi feita verificacao estatica de sintaxe YAML; nao foram executados Docker, Oracle ou testes integrados neste ambiente.
