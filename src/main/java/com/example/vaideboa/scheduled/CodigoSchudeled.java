package com.example.vaideboa.scheduled;

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
