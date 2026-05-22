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
import com.example.vaideboa.service.EmailService;
import com.example.vaideboa.service.NotificacoesService;

@Component
public class NotificacoesSchudeled {
    
    private final CaronaRepository caronaRepository;
    private final NotificacoesService notificacoesService;
    private final EmailService emailService;

    public NotificacoesSchudeled(CaronaRepository caronaRepository, NotificacoesService notificacoesService,
            EmailService emailService) {
        this.caronaRepository = caronaRepository;
        this.notificacoesService = notificacoesService;
        this.emailService = emailService;
    }

    // ta funcionando so falta notificar o dono da carona 
    // notifica as caronas que estão dentro de uma hora do horario de começo dela e garante que ele ainda não foi validada
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
            String token = reserva.getPassageiro().getToken(); // tem que salvar antes o token do expo vindo do front
            token ="hjhsdhs"; 
                // fiz so para simular o token que estar cadastrado
            if (token != null) {

                String tituloEmail = "🚗 Falta 1 hora para sua carona";
                
                String mensagemEmail = """
                    Olá, %s!

                    Sua carona começará em aproximadamente 1 hora.

                    📍 Saída: %s
                    🕒 Horário: %s

                    Confira os detalhes da viagem no aplicativo para evitar atrasos.

                    Boa viagem! 🚗

                    Equipe VaiDeBoa
                    """.formatted(
                        reserva.getPassageiro().getNome(),
                        carona.getRota().getSaidaTexto(),
                        carona.getHora()
                    );

                    emailService.enviarEmail(
                        mensagemEmail,
                        tituloEmail,
                        reserva.getPassageiro().getUsername()
                    );

                    notificacoesService.enviarPushExpo(
                        token,
                        "🚗 Sua carona começa em 1 hora!",
                        "📍 " + carona.getRota().getSaidaTexto() +
                        " • 🕒 " + carona.getHora()
                    );
                }
            }
        
            carona.setNotificado1h(true);
            caronaRepository.save(carona);
        }
    }
    
}
