package com.example.vaideboa.config;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final WebSocketSessionManager sessionManager;

    @EventListener
    public void handleConnect(SessionConnectedEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());

        if (accessor.getUser() == null) {
            System.out.println("SessionConnectedEvent sem usuário autenticado (accessor.getUser() é null)");
            return;
        }

        String username = accessor.getUser().getName();
        System.out.println("WEBSOCKET CONECTADO: " + username + " | session=" + accessor.getSessionId());
        sessionManager.registrar(username, accessor.getSessionId());
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());

        if (accessor.getUser() == null) {
            System.out.println("SessionDisconnectEvent sem usuário autenticado (accessor.getUser() é null)");
            return;
        }

        String username = accessor.getUser().getName();
        System.out.println("WEBSOCKET DESCONECTADO: " + username);
        sessionManager.remover(username);
    }
}