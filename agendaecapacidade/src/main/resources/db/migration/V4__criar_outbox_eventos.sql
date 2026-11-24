CREATE TABLE outbox_eventos (
    id UUID PRIMARY KEY,
    agregado_id UUID NOT NULL,
    tipo_evento VARCHAR(100) NOT NULL,
    payload TEXT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDENTE',
    criado_em TIMESTAMP NOT NULL,
    publicado_em TIMESTAMP,
    tentativas INTEGER NOT NULL DEFAULT 0,
    ultimo_erro TEXT
);

CREATE INDEX idx_outbox_eventos_status_criado
ON outbox_eventos (status, criado_em);