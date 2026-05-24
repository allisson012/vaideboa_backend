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

    // notifica as caronas que estão dentro de uma hora do horario de começo dela e garante que ela ainda não foi validada
@Transactional
@Scheduled(fixedRate = 300000)
public void verificarCaronasEnotificar() {

    LocalDate data = LocalDate.now();
    LocalTime horaAgora = LocalTime.now();
    LocalTime horaEm1h = horaAgora.plusHours(1);

    List<Carona> caronas =
            caronaRepository.findByDataAndHoraBetweenAndNotificado1hFalse(
                    data,
                    horaAgora,
                    horaEm1h
            );

    for (Carona carona : caronas) {

        // motorista
        enviarNotificacao(
                carona.getMotorista().getNome(),
                carona.getMotorista().getUsername(),
                //token = carona.getMotorista().getToken();
                "hjhsdhs", // token mockado
                carona
        );

        // passageiros
        for (Reserva reserva : carona.getReservas()) {

            enviarNotificacao(
                    reserva.getPassageiro().getNome(),
                    reserva.getPassageiro().getUsername(),
                    // token = reserva.getPassageiro().getToken();
                    "hjhsdhs", // token mockado
                    carona
            );
        }

        carona.setNotificado1h(true);
        caronaRepository.save(carona);
    }
}

private void enviarNotificacao(String nome,String email,String token,Carona carona){

    if (token == null) {
        return;
    }

    String titulo = "🚗 Falta 1 hora para sua carona";

    String mensagem = """
            Olá, %s!

            Sua carona começará em aproximadamente 1 hora.

            📍 Saída: %s
            🕒 Horário: %s

            Confira os detalhes da viagem no aplicativo para evitar atrasos.

            Boa viagem! 🚗

            Equipe VaiDeBoa
            """.formatted(
            nome,
            carona.getRota().getSaidaTexto(),
            carona.getHora()
    );

    emailService.enviarEmail(
            mensagem,
            titulo,
            email
    );

    notificacoesService.enviarPushExpo(
            token,
            titulo,
            "📍 " + carona.getRota().getSaidaTexto() +
                    " • 🕒 " + carona.getHora()
    );
}

}