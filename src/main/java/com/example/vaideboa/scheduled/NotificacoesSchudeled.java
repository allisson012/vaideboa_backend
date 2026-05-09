package com.example.vaideboa.scheduled;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.vaideboa.model.Carona;
import com.example.vaideboa.model.Reserva;
import com.example.vaideboa.repository.CaronaRepository;
import com.example.vaideboa.service.NotificacoesService;

@Component
public class NotificacoesSchudeled {
    
    private final CaronaRepository caronaRepository;
    private final NotificacoesService notificacoesService;
    public NotificacoesSchudeled(CaronaRepository caronaRepository, NotificacoesService notificacoesService) {
        this.caronaRepository = caronaRepository;
        this.notificacoesService = notificacoesService;
    }
    // ta funcionando so falta notificar o dono da carona 
    @Transactional
    @Scheduled(fixedRate = 300000)
    public void verificarCaronasEnotificar(){
        LocalDate data = LocalDate.now();
        LocalTime horaAgora = LocalTime.now();
        LocalTime horaEm1h = horaAgora.plusHours(1);
        List<Carona> caronas = caronaRepository.findByDataAndHoraBetweenAndNotificado1hFalse(data, horaAgora, horaEm1h);

        for (Carona carona : caronas) {
            List<Reserva> reservas = carona.getReservas();
            for(Reserva reserva : reservas){
            String token = reserva.getPassageiro().getToken(); 
                if (token == null) {
                    notificacoesService.enviarPushExpo(
                        token,
                        "🚗 Sua carona começa em 1 hora!",
                        "De " + carona.getRota().getSaidaTexto() + " às " + carona.getHora()
                    );
                }
            }
        
            carona.setNotificado1h(true);
            caronaRepository.save(carona);
        }
    }
    
}
