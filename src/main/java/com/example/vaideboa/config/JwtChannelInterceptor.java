package com.example.vaideboa.config;

import java.util.List;
import java.util.Optional;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import com.example.vaideboa.security.JwtService;
import com.example.vaideboa.model.Carona;
import com.example.vaideboa.model.User;
import com.example.vaideboa.repository.CaronaRepository;
import com.example.vaideboa.repository.ReservaRepository;
import com.example.vaideboa.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtChannelInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;
    private final CaronaRepository caronaRepository;
    private final UserRepository userRepository;
    private final ReservaRepository reservaRepository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor
                .getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) return message;

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {

            String authHeader = accessor.getFirstNativeHeader("Authorization");
            System.out.println("CONNECT recebido. Header Authorization presente? " + (authHeader != null));

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                System.out.println("CONNECT rejeitado: token ausente ou mal formatado");
                throw new MessageDeliveryException("Token ausente");
            }

            String token = authHeader.substring(7);

            if (!jwtService.tokenValido(token)) {
                System.out.println("CONNECT rejeitado: token inválido ou expirado");
                throw new MessageDeliveryException("Token inválido ou expirado");
            }

            Jwt jwt = jwtService.decode(token);
            String username = jwt.getSubject();

            System.out.println("CONNECT autenticado com sucesso: " + username);

            UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                    username, null, List.of()
                );

            accessor.setUser(auth);
        }

        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            validarInscricao(accessor);
        }
        if (StompCommand.SEND.equals(accessor.getCommand())) {
            validarSolicitacaoTrajeto(accessor);
        }

        return message;
    }

    private void validarInscricao(StompHeaderAccessor accessor) {
        String destino = accessor.getDestination();
        if (destino == null || !destino.startsWith("/topic/carona/")) {
            return;
        }

        Authentication authentication = (Authentication) accessor.getUser();
        if (authentication == null || !podeAcompanhar(authentication.getName(), extrairIdCarona(destino))) {
            throw new MessageDeliveryException("Usuário não pode acompanhar esta carona");
        }
    }

    private Long extrairIdCarona(String destino) {
        try {
            return Long.valueOf(destino.substring("/topic/carona/".length()));
        } catch (NumberFormatException exception) {
            throw new MessageDeliveryException("Destino de carona inválido");
        }
    }

    private boolean podeAcompanhar(String username, Long idCarona) {
        Optional<User> userOpt = userRepository.findByUsernameAndAtivoTrue(username);
        Optional<Carona> caronaOpt = caronaRepository.findById(idCarona);
        if (userOpt.isEmpty() || caronaOpt.isEmpty()) {
            return false;
        }

        User user = userOpt.get();
        Carona carona = caronaOpt.get();
        return carona.getMotorista().getId().equals(user.getId())
                || reservaRepository.findByCaronaAndPassageiro(carona, user)
                        .map(reserva -> reserva.isAprovado())
                        .orElse(false);
    }

    private void validarSolicitacaoTrajeto(StompHeaderAccessor accessor) {
        String destino = accessor.getDestination();
        String prefixo = "/app/carona/";
        String sufixo = "/trajeto";
        if (destino == null || !destino.startsWith(prefixo) || !destino.endsWith(sufixo)) {
            return;
        }

        String id = destino.substring(prefixo.length(), destino.length() - sufixo.length());
        Authentication authentication = (Authentication) accessor.getUser();
        try {
            if (authentication == null || !podeAcompanhar(authentication.getName(), Long.valueOf(id))) {
                throw new MessageDeliveryException("Usuário não pode acompanhar esta carona");
            }
        } catch (NumberFormatException exception) {
            throw new MessageDeliveryException("Destino de carona inválido");
        }
    }

}
