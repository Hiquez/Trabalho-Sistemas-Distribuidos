CREATE TABLE mensagens_processadas (
    message_id VARCHAR(255) PRIMARY KEY,
    tipo_mensagem VARCHAR(100) NOT NULL,
    processado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);