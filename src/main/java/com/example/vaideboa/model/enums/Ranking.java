package com.example.vaideboa.model.enums;

public enum Ranking {
    NOVATO("Novato"),
    INTERMEDIARIO("Intermediário"),
    EXPERIENTE("Experiente");
    
    private final String descricao;

    Ranking(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
