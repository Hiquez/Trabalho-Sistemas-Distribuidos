CREATE TABLE mensagens_processadas (
    message_id VARCHAR2(255) PRIMARY KEY,
    tipo_mensagem VARCHAR2(100) NOT NULL,
    processado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Controle de eventos que estão sendo publicados
ALTER TABLE outbox_eventos
ADD processamento_id RAW(16);

ALTER TABLE outbox_eventos
ADD processando_em TIMESTAMP;

CREATE INDEX idx_outbox_processamento
ON outbox_eventos (processamento_id);