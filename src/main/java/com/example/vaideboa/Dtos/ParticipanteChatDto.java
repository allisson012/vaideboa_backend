package com.example.vaideboa.Dtos;

public record ParticipanteChatDto(Long idUsuario, String nome, Papel papel, String fotoUrl) {
    public enum Papel {
        MOTORISTA,
        PASSAGEIRO
    }
}
