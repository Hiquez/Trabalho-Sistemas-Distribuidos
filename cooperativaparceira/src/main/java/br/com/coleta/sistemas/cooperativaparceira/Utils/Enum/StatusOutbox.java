package br.com.coleta.sistemas.cooperativaparceira.Utils.Enum;

public enum StatusOutbox {
    EM_ANALISE("Em análise"),
    ACEITO("Aceito"),
    RECUSADO("Recusado"),
    CANCELADO("Cancelado");

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
