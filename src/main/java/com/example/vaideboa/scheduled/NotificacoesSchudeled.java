package com.example.vaideboa.scheduled;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.vaideboa.model.Carona;
import com.example.vaideboa.model.Reserva;
import com.example.vaideboa.model.enums.StatusCarona;
import com.example.vaideboa.model.enums.StatusReserva;
import com.example.vaideboa.repository.CaronaRepository;
import com.example.vaideboa.repository.ReservaRepository;
import com.example.vaideboa.service.NotificacoesService;

@Component
public class NotificacoesSchudeled {

    private final CaronaRepository caronaRepository;
    private final ReservaRepository reservaRepository;
    private final NotificacoesService notificacoesService;
    

    public NotificacoesSchudeled(CaronaRepository caronaRepository, ReservaRepository reservaRepository,
            NotificacoesService notificacoesService) {
        this.caronaRepository = caronaRepository;
        this.reservaRepository = reservaRepository;
        this.notificacoesService = notificacoesService;
}

    @Transactional
    @Scheduled(fixedRate = 300000)
    public void verificarCaronasEnotificar() {

        LocalDate data = LocalDate.now();
        LocalTime horaAgora = LocalTime.now();
        LocalTime horaEm1h = horaAgora.plusHours(1);

        List<Carona> caronas =
                caronaRepository.findByDataAndHoraBetweenAndNotificado1hFalseAndStatusCarona(
                        data,
                        horaAgora,
                        horaEm1h,
                        StatusCarona.EM_ESPERA);

        for (Carona carona : caronas) {

            notificarUsuario(
                    carona.getMotorista().getNome(),
                    carona.getMotorista().getUsername(),
                    carona
            );

            for (Reserva reserva : reservaRepository.findByCaronaAndStatusReserva(carona, StatusReserva.ACEITA)) {

                notificarUsuario(
                        reserva.getPassageiro().getNome(),
                        reserva.getPassageiro().getUsername(),
                        carona
                );
            }

            carona.setNotificado1h(true);
            caronaRepository.save(carona);
        }
    }

    private void notificarUsuario(
            String nome,
            String username,
            Carona carona) {

        String titulo = "🚗 Falta 1 hora para sua carona";

        String email = """
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

        String notificacao = "📍 "
                + carona.getRota().getSaidaTexto()
                + " • 🕒 "
                + carona.getHora();


        notificacoesService.enviarNotificacao(
                username,
                titulo,
                email,
                notificacao
        );
    }
}
