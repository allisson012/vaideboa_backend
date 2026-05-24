package com.example.vaideboa.model.enums;

public enum StatusReserva {
    PENDENTE("Pendente"),
    EMBARQUE_LIBERADO("Embarque Liberado"),
    EMBARCADO("Embarcado"),
    CONCLUIDA_USUARIO("Concluida pelo usuario"),
    CONCLUIDA_SISTEMA("Concluida pelo sistema"),
    NAO_COMPARECEU("Não compareceu");

    private final String descricao;

    StatusReserva(String descricao){
        this.descricao = descricao;
    }

    public String getDescricao(){
        return this.descricao;
    }
}