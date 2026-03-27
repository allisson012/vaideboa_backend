package com.example.vaideboa.model.enums;

public enum NivelPreferencia {
    NUNCA("Nunca"),
    TALVEZ("Talvez"),
    SEM_PROBLEMA("Sem problema");

    private final String descricao;

    NivelPreferencia(String descricao){
        this.descricao = descricao;
    }

    public String getDescricao(){
        return descricao;
    }
}
