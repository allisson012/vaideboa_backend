package com.example.vaideboa.model.enums;

public enum TipoEvento {
    EMBARQUE_LIBERADO("Embarque Liberado"),
    EMBARCADO("Embarcado"),
    DESEMBARQUE_LIBERADO("Desembarque Liberado"),
    DESEMBARCADO("Desembarcado"),
    NAO_COMPARECEU("Não Compareceu");

    private final String descricao;

    TipoEvento(String descricao){
        this.descricao = descricao;
    }

    public String getDescricao(){
        return this.descricao;
    }
}
