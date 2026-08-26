package com.example.vaideboa.model.enums;

public enum StatusCompartilhamento {
    EM_ANDAMENTO("Em andamento"),
    FINALIZADO("Finalizado"),
    CANCELADO("Cancelado");
    
    private final String descricao;

    StatusCompartilhamento(String descricao){
        this.descricao = descricao;
    }

    public String getDescricao(){
        return this.descricao;
    }
}
