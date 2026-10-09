package com.example.vaideboa.service;

import java.time.format.DateTimeFormatter;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import com.example.vaideboa.config.WebSocketSessionManager;
import com.example.vaideboa.event.NotificacaoCancelamentoCaronaEvent;
import com.example.vaideboa.model.Carona;
import com.example.vaideboa.model.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificacoesService {

    private final WebSocketSessionManager sessionManager;
    private final WebSocketNotificationService webSocketNotificationService;
    private final ExpoNotificationService expoNotificationService;
    private final EmailService emailService;
    private final ApplicationEventPublisher eventPublisher;

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
    public void enviarNotificacaoCancelamentoCarona(User user, Carona carona){
        String titulo = "Carona cancelada pelo motorista";
        String data = carona.getData().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        String hora = carona.getHora().format(DateTimeFormatter.ofPattern("HH:mm"));

        String mensagemEmail = """
                Olá, %s!

                O motorista cancelou a carona abaixo:

                Saída: %s
                Destino: %s
                Data: %s
                Horário: %s

                Essa carona não será realizada. Se você tinha um pedido pendente ou uma
                reserva confirmada, sua participação nessa carona foi cancelada.

                Acesse o aplicativo para buscar outra carona para o seu trajeto.

                Equipe VaiDeBoa
                """.formatted(
                user.getNome(),
                carona.getRota().getSaidaTexto(),
                carona.getRota().getDestinoTexto(),
                data,
                hora
        );

        String mensagemPush = "Sua carona de %s para %s, em %s às %s, foi cancelada pelo motorista."
                .formatted(
                        carona.getRota().getSaidaTexto(),
                        carona.getRota().getDestinoTexto(),
                        data,
                        hora
                );

        // Os textos são preparados na transação para não acessar entidades JPA após o commit.
        eventPublisher.publishEvent(new NotificacaoCancelamentoCaronaEvent(
                user.getUsername(), titulo, mensagemEmail, mensagemPush));

    }
}
