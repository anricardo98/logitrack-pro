package br.com.logitrack.domain;

public enum StatusManutencao {
    PENDENTE("Pendente"),
    EM_REALIZACAO("Em realização"),
    CONCLUIDA("Concluída");

    private final String descricao;

    StatusManutencao(String descricao) { this.descricao = descricao; }

    public String getDescricao() { return descricao; }
}
