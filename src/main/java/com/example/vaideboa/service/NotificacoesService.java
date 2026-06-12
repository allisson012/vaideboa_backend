package com.example.vaideboa.service;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.example.vaideboa.config.WebSocketSessionManager;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificacoesService {

    private final SimpMessagingTemplate messagingTemplate;
    private final WebSocketSessionManager sessionManager;
    private final ExpoNotificationService expoNotificationService;

    public void enviarPushExpo(String token, String titulo, String mensagem){
        System.out.println("Enviando notificação com " + titulo  + " mensagem = " +mensagem);
    }

    public void enviarNotificacao(String username, String mensagem) {

        if (sessionManager.estaOnline(username)) {
            // app aberto → envia pelo WS
            messagingTemplate.convertAndSendToUser(
                username,
                "/queue/notificacoes",
                mensagem
            );
        } else {
            // app fechado background → envia pelo Expo Push
            expoNotificationService.enviar(username, mensagem, "corpo");
        }
    }
}
