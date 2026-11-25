CREATE TABLE mensagens_processadas (
    message_id VARCHAR2(255) PRIMARY KEY,
    tipo_mensagem VARCHAR2(100) NOT NULL,
    processado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);