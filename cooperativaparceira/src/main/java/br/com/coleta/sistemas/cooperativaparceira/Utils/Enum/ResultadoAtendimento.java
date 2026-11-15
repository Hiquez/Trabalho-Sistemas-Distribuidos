package br.com.coleta.sistemas.cooperativaparceira.Utils.Enum;

public enum ResultadoAtendimento {
    EM_ANALISE("Em análise"),
    ACEITO("Aceito"),
    RECUSADO("Recusado"),
    CANCELADO("Cancelado");

    private String descricao;

    ResultadoAtendimento(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
}
