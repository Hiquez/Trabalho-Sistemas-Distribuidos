CREATE TABLE outbox_eventos (
    id RAW(16) PRIMARY KEY,
    agregado_id RAW(16) NOT NULL,
    tipo_evento VARCHAR2(100) NOT NULL,
    payload CLOB NOT NULL,
    status VARCHAR2(30) DEFAULT 'PENDENTE' NOT NULL,
    criado_em TIMESTAMP NOT NULL,
    publicado_em TIMESTAMP,
    tentativas INTEGER DEFAULT 0 NOT NULL,
    ultimo_erro CLOB
);

CREATE INDEX idx_outbox_eventos_status_criado
ON outbox_eventos (status, criado_em);