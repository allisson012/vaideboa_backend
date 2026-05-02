package com.example.vaideboa.model.enums;

public enum StatusCarona {
    EM_ESPERA("Em espera"),
    EM_ANDAMENTO("Em andamento"),
    CONCLUIDA("Concluída"),
    CANCELADA("Cancelada");


    private final String descricao;

    StatusCarona(String descricao){
        this.descricao = descricao;
    }

    public String getDescricao(){
        return this.descricao;
    }
}
