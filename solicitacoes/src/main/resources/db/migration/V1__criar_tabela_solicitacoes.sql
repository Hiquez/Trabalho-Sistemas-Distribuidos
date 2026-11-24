CREATE TABLE solicitacoes (
    id UUID PRIMARY KEY,
    bairro VARCHAR(255) NOT NULL,
    data_coleta DATE NOT NULL,
    status VARCHAR(50) NOT NULL,
    criado_em TIMESTAMP NOT NULL
);