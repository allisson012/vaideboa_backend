package com.example.vaideboa.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.vaideboa.Dtos.ApiResponse;
import com.example.vaideboa.Dtos.ConfirmarCodigoDto;
import com.example.vaideboa.model.Carona;
import com.example.vaideboa.model.Reserva;
import com.example.vaideboa.model.ReservaCodigo;
import com.example.vaideboa.model.ReservaEvento;
import com.example.vaideboa.model.User;
import com.example.vaideboa.model.enums.StatusReserva;
import com.example.vaideboa.model.enums.TipoEvento;
import com.example.vaideboa.repository.CaronaRepository;
import com.example.vaideboa.repository.ReservaCodigoRepository;
import com.example.vaideboa.repository.ReservaEventoRepository;
import com.example.vaideboa.repository.ReservaRepository;
import com.example.vaideboa.repository.UserRepository;

@Service
public class CodigoService {
    private final ReservaCodigoRepository reservaCodigoRepository;
    private final ReservaRepository reservaRepository;
    private final EmailService emailService;
    private final CaronaRepository caronaRepository;
    private final UserRepository userRepository;
    private final ReservaEventoRepository reservaEventoRepository;

    public CodigoService(ReservaCodigoRepository reservaCodigoRepository, ReservaRepository reservaRepository,
            EmailService emailService, CaronaRepository caronaRepository, UserRepository userRepository,
            ReservaEventoRepository reservaEventoRepository) {
        this.reservaCodigoRepository = reservaCodigoRepository;
        this.reservaRepository = reservaRepository;
        this.emailService = emailService;
        this.caronaRepository = caronaRepository;
        this.userRepository = userRepository;
        this.reservaEventoRepository = reservaEventoRepository;
    }
    @Transactional
    public ApiResponse gerarCodigos(List<Reserva> reservas){
        SecureRandom random = new SecureRandom();

        try {
            List<ReservaCodigo> codigos = new ArrayList<>();
            List<ReservaEvento> reservasEventos = new ArrayList<>();
            Set<String> usados = new HashSet<>();

            for (Reserva reserva : reservas) {

                String codigoEmbarque;
                String codigoDesembarque;

                do {
                    codigoEmbarque = String.format("%04d", random.nextInt(10000));
                } while (usados.contains(codigoEmbarque));

                usados.add(codigoEmbarque);

                do {
                    codigoDesembarque = String.format("%04d", random.nextInt(10000));
                } while (usados.contains(codigoDesembarque));

                usados.add(codigoDesembarque);

                ReservaCodigo reservaCodigo = new ReservaCodigo();
                reservaCodigo.setCodigoEmbarque(codigoEmbarque);
                enviarCodigo(reserva.getPassageiro().getNome(), codigoEmbarque, reserva.getPassageiro().getUsername());
                reservaCodigo.setEmbarqueLiberado(true);
                reservaCodigo.setCodigoDesembarque(codigoDesembarque);
                reservaCodigo.setCriadoEm(LocalDateTime.now());
                ReservaEvento reservaEvento = new ReservaEvento();
                reservaEvento.setCriadoEm(LocalDateTime.now());
                reservaEvento.setReserva(reserva);
                reservaEvento.setTipoEvento(TipoEvento.EMBARQUE_LIBERADO);
                reservaCodigo.setReserva(reserva);
                reserva.setReservaCodigo(reservaCodigo);
                reservasEventos.add(reservaEvento);
                codigos.add(reservaCodigo);

                reserva.setJaEnviadoCodigoInicio(true);
            }

            reservaCodigoRepository.saveAll(codigos);
            reservaEventoRepository.saveAll(reservasEventos);
            reservaRepository.saveAll(reservas);

            return new ApiResponse(true, "Códigos gerados com sucesso");

        }catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse(false, "Erro ao gerar códigos");
        }
    }

    @Transactional
    public ApiResponse confirmarCodigo(ConfirmarCodigoDto dto, String username){
        Optional<User> userOpt = userRepository.findByUsernameAndAtivoTrue(username);
        if(userOpt.isEmpty()){
            return new ApiResponse(false,"Usuário não encontrado");
        }
        User user = userOpt.get();
        Optional<Carona> caronaOpt = caronaRepository.findById(dto.getIdCarona());
        if(caronaOpt.isEmpty()){
            return new ApiResponse(false, "Carona não encontrada");
        }
        Carona carona = caronaOpt.get();
        Optional<Reserva> reservaOpt = reservaRepository.findByCaronaAndPassageiro(carona, user);
        if(reservaOpt.isEmpty()){
            return new ApiResponse(false, "Reserva não encontrada");
        }
        Reserva reserva = reservaOpt.get();
        if(reserva.getReservaCodigo() == null){
            return new ApiResponse(false,"Nenhum código foi gerado para essa reserva");
        }
        ReservaCodigo reservaCodigo = reserva.getReservaCodigo();
        if(!reservaCodigo.isEmbarqueRealizado()){
            if(!reservaCodigo.getCodigoEmbarque().equals(dto.getCodigo())){
                return new ApiResponse(false,"Codigo informado não bate com o codigo de embarque!");
            }
            reservaCodigo.setEmbarqueRealizado(true);
            reserva.setStatusReserva(StatusReserva.EMBARCADO);
            ReservaEvento reservaEvento = new ReservaEvento();
            reservaEvento.setCriadoEm(LocalDateTime.now());
            reservaEvento.setTipoEvento(TipoEvento.EMBARCADO);
            reservaEvento.setReserva(reserva);
            reservaCodigo.setDesembarqueLiberado(true);
            reservaEventoRepository.save(reservaEvento);
            reservaCodigoRepository.save(reservaCodigo);
            reservaRepository.save(reserva);
            // enviar email o codigo de desembarque agora
            enviarCodigoDesembarque(user.getNome(), reservaCodigo.getCodigoDesembarque(), user.getUsername());
            return new ApiResponse(true, "Codigo de embarque validado com sucesso");
        }
        if(reservaCodigo.isDesembarqueRealizado()){
            return new ApiResponse(false,"Desembarque já confirmado");
        }
        if(!reservaCodigo.getCodigoDesembarque().equals(dto.getCodigo())){
            return new ApiResponse(false, "Codigo informado não bate com o codigo de desembarque!");
        }
        reservaCodigo.setDesembarqueRealizado(true);
        reserva.setStatusReserva(StatusReserva.CONCLUIDA_USUARIO);
        ReservaEvento reservaEvento = new ReservaEvento();
        reservaEvento.setCriadoEm(LocalDateTime.now());
        reservaEvento.setTipoEvento(TipoEvento.DESEMBARCADO);
        reservaEvento.setReserva(reserva);
        reservaEventoRepository.save(reservaEvento);
        reservaCodigoRepository.save(reservaCodigo);
        reservaRepository.save(reserva);
        return new ApiResponse(true, "Codigo de desembarque validado com sucesso");
    }

    private void enviarCodigo(String nome, String codigo, String emailUsuario){
        String assunto = "🚗 Código de embarque da sua carona";

        String mensagem = """
            Olá, %s!

            Sua carona está pronta para começar.

            🔐 Código de embarque: %s

            ⚠️ IMPORTANTE:
            Informe este código somente após entrar no veículo.

            O código confirma sua presença na carona.
            Caso ele não seja informado, sua reserva poderá ser marcada como não compareceu.

            Após informar o código, a presença na carona será considerada confirmada pelo sistema.
            Portanto, não informe o código antes de realmente entrar no veículo.

            Boa viagem! 🚗

            Equipe VaiDeBoa
            """.formatted(
                nome,
                codigo
        );

        emailService.enviarEmail(
            mensagem,
            assunto,
            emailUsuario
        );
        // posso enviar mensagem pelo expo notification tambem
    }
    private void enviarCodigoDesembarque(String nome, String codigo, String emailUsuario){

        String assunto = "🚗 Código de desembarque da sua carona";

        String mensagem = """
            Olá, %s!

            Seu embarque foi confirmado com sucesso. ✅

            🔐 Código de desembarque: %s

            Informe este código ao finalizar sua viagem caso deseje confirmar o desembarque manualmente no aplicativo.

            Caso o código não seja informado, a carona poderá ser concluída automaticamente pelo sistema após algum tempo.

            Obrigado por utilizar o VaiDeBoa! 🚗

            Equipe VaiDeBoa
            """.formatted(
                nome,
                codigo
            );

            emailService.enviarEmail(
                mensagem,
                assunto,
                emailUsuario
            );
        // posso enviar push notification também
    }
}
