package com.example.vaideboa.Dtos;

public class RecuperarSenhaDto {
    private final String senha;
    private final String confirmarSenha;
    private final String tokenReset;
    public RecuperarSenhaDto(String senha, String confirmarSenha, String tokenReset) {
        this.senha = senha;
        this.confirmarSenha = confirmarSenha;
        this.tokenReset = tokenReset;
    }
    public String getSenha() {
        return senha;
    }
    public String getConfirmarSenha() {
        return confirmarSenha;
    }
    public String getTokenReset() {
        return tokenReset;
    }
}
