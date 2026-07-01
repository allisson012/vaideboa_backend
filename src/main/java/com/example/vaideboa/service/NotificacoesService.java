package com.example.vaideboa.service;

import org.springframework.stereotype.Service;
import com.example.vaideboa.config.WebSocketSessionManager;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificacoesService {

    private final WebSocketSessionManager sessionManager;
    private final WebSocketNotificationService webSocketNotificationService;
    private final ExpoNotificationService expoNotificationService;
    private final EmailService emailService;

    public void enviarNotificacao(String username,String titulo,String mensagemEmail , String mensagemPush) {
        
        if (sessionManager.estaOnline(username)) {
            // web socket 
            webSocketNotificationService.enviar(username, mensagemPush);
        }
        else {
            expoNotificationService.enviar(
                    username,
                    titulo,
                    mensagemPush
            );
        }
        // email -> sempre envia no email
        emailService.enviarEmail(mensagemEmail, titulo, username);
    }
}