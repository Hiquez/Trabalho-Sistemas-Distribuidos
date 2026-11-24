CREATE TABLE mensagens_processadas (
    message_id VARCHAR(255) PRIMARY KEY,
    tipo_mensagem VARCHAR(100) NOT NULL,
    processado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Controle de eventos que estão sendo publicados
ALTER TABLE outbox_eventos
ADD COLUMN processamento_id UUID;

ALTER TABLE outbox_eventos
ADD COLUMN processando_em TIMESTAMP;

CREATE INDEX idx_outbox_processamento
ON outbox_eventos (processamento_id);