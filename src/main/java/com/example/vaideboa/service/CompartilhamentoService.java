package com.example.vaideboa.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.locationtech.jts.geom.Coordinate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.example.vaideboa.Dtos.LocalizacaoDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CompartilhamentoService {

    private final SimpMessagingTemplate messagingTemplate;
    private final Map<Long, List<Coordinate>> trajetosEmAndamento = new ConcurrentHashMap<>();
    public void atualizarLocalizacao(Long idCarona, double latitude, double longitude) {
        System.out.println("latidute = "+latitude);
        System.out.println("longitude = "+longitude);
        LocalizacaoDto localizacao = new LocalizacaoDto(latitude, longitude);
        messagingTemplate.convertAndSend("/topic/carona/" + idCarona, localizacao);
        // não repetir o mesmo ponto
    }
    public void iniciarCompartilhamento(Long idCarona) {
        trajetosEmAndamento.put(idCarona, new ArrayList<>());
    }

    public void removerCompartilhamento(Long idCarona){

    }

    public void finalizarCompartilhamento(Long idCarona){
        
    }
}