package com.example.vaideboa.event;

public record NotificacaoCancelamentoCaronaEvent(
        String username,
        String titulo,
        String mensagemEmail,
        String mensagemPush) {
}
