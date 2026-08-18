package com.example.vaideboa.service;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.example.vaideboa.Dtos.LocalizacaoDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CompartilhamentoService {

    private final SimpMessagingTemplate messagingTemplate;

    public void atualizarLocalizacao(
            Long idCarona,
            double latitude,
            double longitude
    ) {

        LocalizacaoDto localizacao =
                new LocalizacaoDto(latitude, longitude);

        messagingTemplate.convertAndSend(
                "/topic/carona/" + idCarona,
                localizacao
        );
    }
}