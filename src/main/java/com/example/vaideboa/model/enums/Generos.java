package com.example.vaideboa.model.enums;

public enum Generos {
    MASCULINO("Masculino"),
    FEMININO("Feminino"),
    OUTROS("Outros"),
    NAO_INFORMADO("Não informado");

    private final String descricao;

    Generos(String descricao){
        this.descricao = descricao;
    }

    public String getDescricao(){
      return this.descricao;
    }
}
