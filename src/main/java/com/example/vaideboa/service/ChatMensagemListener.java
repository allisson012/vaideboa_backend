package com.example.vaideboa.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.example.vaideboa.event.MensagemCriadaEvent;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ChatMensagemListener {

    private static final Logger logger = LoggerFactory.getLogger(ChatMensagemListener.class);
    private final SimpMessagingTemplate messagingTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void enviarMensagem(MensagemCriadaEvent event) {
        try {
            messagingTemplate.convertAndSend(
                    "/topic/chat/" + event.mensagem().idReserva(),
                    event.mensagem());
        } catch (MessagingException exception) {
            // A mensagem já foi salva e pode ser recuperada pelo histórico.
            logger.error("Não foi possível publicar a mensagem {} no WebSocket",
                    event.mensagem().id(), exception);
        }
    }
}
