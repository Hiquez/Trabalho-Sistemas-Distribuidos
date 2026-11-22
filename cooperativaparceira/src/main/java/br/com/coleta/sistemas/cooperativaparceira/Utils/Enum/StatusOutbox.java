package br.com.coleta.sistemas.cooperativaparceira.Utils.Enum;

public enum StatusOutbox {
    PENDENTE("Pendente"),
    PUBLICADO("Publicado"),
    PROCESSANDO("Processando");

    private String descricao;

    StatusOutbox(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
}
