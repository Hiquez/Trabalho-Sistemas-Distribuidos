ALTER TABLE outbox_eventos
ADD COLUMN processamento_id UUID,
ADD COLUMN processando_em TIMESTAMP;

CREATE INDEX idx_outbox_processamento
ON outbox_eventos (processamento_id);