package com.example.vaideboa.service;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WebSocketNotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    public void enviar(
            String username,
            String mensagem){

        messagingTemplate.convertAndSendToUser(
                username,
                "/queue/notificacoes",
                mensagem
        );

    }

}