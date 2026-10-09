package com.example.vaideboa.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.example.vaideboa.event.NotificacaoCancelamentoCaronaEvent;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CaronaCanceladaListener {

    private static final Logger logger = LoggerFactory.getLogger(CaronaCanceladaListener.class);
    private final NotificacoesService notificacoesService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void enviarNotificacao(NotificacaoCancelamentoCaronaEvent event) {
        try {
            notificacoesService.enviarNotificacao(
                    event.username(), event.titulo(), event.mensagemEmail(), event.mensagemPush());
        } catch (Exception exception) {
            // O cancelamento já foi confirmado; a falha não deve interromper os demais envios.
            logger.error("Não foi possível notificar o cancelamento da carona para {}",
                    event.username(), exception);
        }
    }
}
