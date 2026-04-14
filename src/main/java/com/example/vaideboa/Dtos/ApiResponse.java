package com.example.vaideboa.Dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse {
    private boolean retorno;
    private String mensagem;
    private Object dados;

    public ApiResponse(boolean retorno, String mensagem) {
        this.retorno = retorno;
        this.mensagem = mensagem;
        this.dados = null;
    }
}
