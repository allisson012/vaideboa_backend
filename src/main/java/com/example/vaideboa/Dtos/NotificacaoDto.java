package com.example.vaideboa.Dtos;

public class NotificacaoDto {
    private final String titulo;
    private final String mensagem;
    
    public NotificacaoDto(String titulo, String mensagem) {
        this.titulo = titulo;
        this.mensagem = mensagem;
    }

    public String getTitulo() {
        return titulo;
    }
    public String getMensagem() {
        return mensagem;
    }
    
}
