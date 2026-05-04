package com.example.vaideboa.scheduled;

import java.util.List;

import org.locationtech.jts.geom.Point;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.vaideboa.Dtos.CaronaEmAndamentoDTO;
import com.example.vaideboa.repository.CaronaRepository;

//@Component
public class CodigoSchudeled {
    private final CaronaRepository caronaRepository;

    public CodigoSchudeled(CaronaRepository caronaRepository) {
        this.caronaRepository = caronaRepository;
    }
    
    // @Scheduled(fixedRate = 1000) // 1 minuto
    // public void verificarCaronaseEnviarCodigos(Point localizacaoMotorista){
    //    List<CaronaEmAndamentoDTO> dtos = caronaRepository.buscarProximosParaEnviarCodigo(localizacaoMotorista);
    // }
}
