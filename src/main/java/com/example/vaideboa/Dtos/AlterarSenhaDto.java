package com.example.vaideboa.Dtos;

public class AlterarSenhaDto {
    
    private final String senhaAtual;
    private final String novaSenha;
    private final String confirmarSenha;
    public AlterarSenhaDto(String senhaAtual, String novaSenha, String confirmarSenha) {
        this.senhaAtual = senhaAtual;
        this.novaSenha = novaSenha;
        this.confirmarSenha = confirmarSenha;
    }
    public String getSenhaAtual() {
        return senhaAtual;
    }
    public String getNovaSenha() {
        return novaSenha;
    }
    public String getConfirmarSenha() {
        return confirmarSenha;
    }
    
}
