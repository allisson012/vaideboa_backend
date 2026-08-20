package com.example.vaideboa.config;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component
public class WebSocketSessionManager {

    private final Map<String, String> usuariosOnline = new ConcurrentHashMap<>();

    public void registrar(String username, String sessionId) {
        usuariosOnline.put(username, sessionId);
    }

    public void remover(String username) {
        usuariosOnline.remove(username);
    }

    public boolean estaOnline(String username) {
        return usuariosOnline.containsKey(username);
    }
}