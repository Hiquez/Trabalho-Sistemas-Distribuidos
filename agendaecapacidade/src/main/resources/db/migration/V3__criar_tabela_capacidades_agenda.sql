CREATE TABLE capacidades_agenda (
    id RAW(16) PRIMARY KEY,
    bairro VARCHAR2(255) NOT NULL,
    data_coleta DATE NOT NULL,
    capacidade_total INTEGER NOT NULL,
    vagas_reservadas INTEGER DEFAULT 0 NOT NULL,

    CONSTRAINT uk_bairro_data
        UNIQUE (bairro, data_coleta),

    CONSTRAINT chk_capacidade_positiva
        CHECK (capacidade_total > 0),

    CONSTRAINT chk_vagas_reservadas
        CHECK (
            vagas_reservadas >= 0
            AND vagas_reservadas <= capacidade_total
        )
);
