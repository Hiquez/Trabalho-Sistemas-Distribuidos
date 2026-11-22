package br.com.coleta.sistemas.cooperativaparceira.Utils.Enum;

public enum StatusCompromisso {
    EM_ANALISE("Em análise"),
    ACEITO("Aceito"),
    RECUSADO("Recusado"),
    CANCELADO("Cancelado");

    private String descricao;

    StatusCompromisso(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
