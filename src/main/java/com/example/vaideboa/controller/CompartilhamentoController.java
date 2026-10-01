package com.example.vaideboa.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import com.example.vaideboa.Dtos.LocalizacaoDto;
import com.example.vaideboa.service.CompartilhamentoService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class CompartilhamentoController {

    private final CompartilhamentoService compartilhamentoService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/carona/{idCarona}/localizacao")
    public void atualizarLocalizacao(@DestinationVariable Long idCarona, LocalizacaoDto localizacao, Authentication auth) {
        compartilhamentoService.atualizarLocalizacao(idCarona, localizacao.getLatitude(), localizacao.getLongitude(), auth.getName());
    }

    @MessageMapping("/carona/{idCarona}/trajeto")
    public void solicitarTrajetoAtual(@DestinationVariable Long idCarona, Principal principal) {
        List<LocalizacaoDto> trajeto = compartilhamentoService.obterTrajetoAtual(idCarona);

        messagingTemplate.convertAndSendToUser(
            principal.getName(),
            "/queue/carona/" + idCarona + "/trajeto",
            trajeto
        );
    }
}
