package com.example.vaideboa.scheduled;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.vaideboa.repository.CaronaRepository;

@Component
public class CodigoSchudeled {
    private final CaronaRepository caronaRepository;

    public CodigoSchudeled(CaronaRepository caronaRepository) {
        this.caronaRepository = caronaRepository;
    }

    @Scheduled(fixedRate = 1000) // 1 minuto
    public void verificarCaronaseEnviarCodigos(){

    }
}
