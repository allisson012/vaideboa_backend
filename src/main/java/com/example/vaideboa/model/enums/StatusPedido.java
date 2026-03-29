package com.example.vaideboa.model.enums;

public enum StatusPedido {
    PENDENTE("Pendente"),
    ACEITO("Aceito"),
    RECUSADO("Recusado"),
    CANCELADO("Cancelado");


    private final String descricao;

    StatusPedido(String descricao){
        this.descricao = descricao;
    }

    public String getDescricao(){
        return this.descricao;
    }
}
