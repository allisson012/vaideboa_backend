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
import com.example.vaideboa.model.enums.StatusCarona;
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
        // Usa os últimos quatro dígitos do telefone na ordem das reservas, sem repetir códigos.
        try {
            List<ReservaCodigo> codigos = new ArrayList<>();
            List<ReservaEvento> reservasEventos = new ArrayList<>();
            Set<String> usados = new HashSet<>();

            for (Reserva reserva : reservas) {
                if (reserva.getStatusReserva() != StatusReserva.CANCELADA && reserva.getReservaCodigo() != null) {
                    usados.add(reserva.getReservaCodigo().getCodigoEmbarque());
                }
            }

            for (Reserva reserva : reservas) {
                if (reserva.getStatusReserva() == StatusReserva.CANCELADA || reserva.getReservaCodigo() != null) {
                    continue;
                }
                User passageiro = reserva.getPassageiro();
                String codigoEmbarque = null;
                if (passageiro.getTelefone() != null && passageiro.getTelefone().matches("[0-9]{11}")){
                    String telefone = passageiro.getTelefone();
                    codigoEmbarque = telefone.substring(telefone.length() - 4);
                }
                if (codigoEmbarque == null || usados.contains(codigoEmbarque)) {
                    do {
                        codigoEmbarque = String.format("%04d", random.nextInt(10000));
                    } while (usados.contains(codigoEmbarque));
                }
                usados.add(codigoEmbarque);

                ReservaCodigo reservaCodigo = new ReservaCodigo();
                reservaCodigo.setCodigoEmbarque(codigoEmbarque);
                enviarCodigo(reserva.getPassageiro().getNome(), codigoEmbarque, reserva.getPassageiro().getUsername());
                reservaCodigo.setEmbarqueLiberado(true);
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

            if (codigos.isEmpty()) {
                return new ApiResponse(true, "Códigos já foram gerados para as reservas");
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
        // quem vai confirmar é o motorista
        if (dto == null || dto.getIdCarona() == null || dto.getCodigo() == null || !dto.getCodigo().matches("[0-9]{4}")) {
            return new ApiResponse(false, "Informe a carona e um código de quatro dígitos");
        }
        Optional<User> userOpt = userRepository.findByUsernameAndAtivoTrue(username);
        if(userOpt.isEmpty()){
            return new ApiResponse(false,"Usuário não encontrado");
        }
        User user = userOpt.get();
        Optional<Carona> caronaOpt = caronaRepository.buscarPorIdComBloqueio(dto.getIdCarona());
        if(caronaOpt.isEmpty()){
            return new ApiResponse(false, "Carona não encontrada");
        }
        Carona carona = caronaOpt.get();
        if(!carona.getMotorista().getId().equals(user.getId())){
            return new ApiResponse(false, "Somente o motorista pode confirmar o codigo");
        }
        if (carona.getStatusCarona() != StatusCarona.EM_ANDAMENTO) {
            return new ApiResponse(false, "O embarque só pode ser confirmado com a carona em andamento");
        }
        List<Reserva> reservas = carona.getReservas();
        if(reservas.isEmpty()){
            return new ApiResponse(false,"Essa carona não possui reservas");
        }
        for (Reserva reserva : reservas) {
            if(reserva.getStatusReserva() == StatusReserva.CANCELADA || reserva.getReservaCodigo() == null){
                continue;
            }
            ReservaCodigo reservaCodigo = reserva.getReservaCodigo();
            if(!dto.getCodigo().equals(reservaCodigo.getCodigoEmbarque())){
                continue;
            }
            if(reservaCodigo.isEmbarqueRealizado()){
                return new ApiResponse(false, "Embarque já confirmado");
            }
            if (reserva.getStatusReserva() != StatusReserva.ACEITA && reserva.getStatusReserva() != StatusReserva.EMBARQUE_LIBERADO) {
                return new ApiResponse(false, "A reserva não permite confirmar o embarque no estado atual");
            }
            if (!reservaCodigo.isEmbarqueLiberado()) {
                return new ApiResponse(false, "O embarque ainda não foi liberado");
            }
            reservaCodigo.setEmbarqueRealizado(true);
            reserva.setStatusReserva(StatusReserva.EMBARCADO);
            ReservaEvento reservaEvento = new ReservaEvento();
            reservaEvento.setCriadoEm(LocalDateTime.now());
            reservaEvento.setTipoEvento(TipoEvento.EMBARCADO);
            reservaEvento.setReserva(reserva);
            reservaEventoRepository.save(reservaEvento);
            reservaCodigoRepository.save(reservaCodigo);
            reservaRepository.save(reserva);
            return new ApiResponse(true, "Código de embarque validado com sucesso");
        }
        return new ApiResponse(false,"Codigo informado não esta presente na carona");
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
}
