package com.example.vaideboa.Dtos;

import java.time.LocalDateTime;

import com.example.vaideboa.model.Mensagem;

public record MensagemRetornoDto(
        Long id,
        Long idReserva,
        Long idAutor,
        String mensagem,
        LocalDateTime enviadoEm) {

    public static MensagemRetornoDto from(Mensagem mensagem) {
        return new MensagemRetornoDto(
                mensagem.getId(),
                mensagem.getChat().getReserva().getId(),
                mensagem.getAutor().getId(),
                mensagem.getMensagem(),
                mensagem.getEnviadoEm());
    }
}
