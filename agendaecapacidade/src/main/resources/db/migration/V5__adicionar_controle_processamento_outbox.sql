ALTER TABLE outbox_eventos
ADD (processamento_id RAW(16), processando_em TIMESTAMP);

CREATE INDEX idx_outbox_processamento
ON outbox_eventos (processamento_id);