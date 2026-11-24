CREATE TABLE agendas (
    id UUID PRIMARY KEY,
    solicitacao_id UUID NOT NULL UNIQUE,
    bairro VARCHAR(255) NOT NULL,
    data_coleta DATE NOT NULL,
    status VARCHAR(50) NOT NULL,
    criado_em TIMESTAMP NOT NULL
);
