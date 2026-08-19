package com.example.vaideboa.controller;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import com.example.vaideboa.Dtos.LocalizacaoDto;
import com.example.vaideboa.service.CompartilhamentoService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class CompartilhamentoController {

    private final CompartilhamentoService compartilhamentoService;

    @MessageMapping("/carona/{idCarona}/localizacao")
    public void atualizarLocalizacao(@DestinationVariable Long idCarona,LocalizacaoDto localizacao, Authentication auth) {
        System.out.println("USUÁRIO: " + auth.getName());
        System.out.println("CARONA: " + idCarona);
        System.out.println("LOCALIZAÇÃO: " + localizacao);
        compartilhamentoService.atualizarLocalizacao(
                idCarona,
                localizacao.getLatitude(),
                localizacao.getLongitude()
        );
    }
}