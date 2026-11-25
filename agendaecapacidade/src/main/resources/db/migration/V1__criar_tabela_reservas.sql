CREATE TABLE agendas (
    id RAW(16) PRIMARY KEY,
    solicitacao_id RAW(16) NOT NULL UNIQUE,
    bairro VARCHAR2(255) NOT NULL,
    data_coleta DATE NOT NULL,
    status VARCHAR2(50) NOT NULL,
    criado_em TIMESTAMP NOT NULL
);
