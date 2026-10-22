package com.example.vaideboa.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.transaction.Transactional;

import com.example.vaideboa.Dtos.ApiResponse;
import com.example.vaideboa.Dtos.BuscaCaronaDto;
import com.example.vaideboa.Dtos.BuscaRetornaDto;
import com.example.vaideboa.Dtos.PontoParadaRetornoDto;
import com.example.vaideboa.Dtos.RankingDto;
import com.example.vaideboa.model.Carona;
import com.example.vaideboa.model.PontoParada;
import com.example.vaideboa.model.Reserva;
import com.example.vaideboa.model.Rota;
import com.example.vaideboa.model.User;
import com.example.vaideboa.model.enums.StatusCarona;
import com.example.vaideboa.model.enums.StatusReserva;
import com.example.vaideboa.repository.CaronaRepository;
import com.example.vaideboa.repository.ReservaRepository;
import com.example.vaideboa.repository.RotaRepository;
import com.example.vaideboa.repository.UserRepository;

@Service
public class ReservaService {
    private static final Logger logger = LoggerFactory.getLogger(ReservaService.class);
    @Autowired
    private EmailService emailService;
    private final AvaliacaoService avaliacaoService;
    private final RotaRepository rotaRepository;
    private final CaronaRepository caronaRepository;
    private final GeometryFactory geometryFactory = new GeometryFactory();
    private final RotaMapperService rotaMapperService;
    private final UserRepository userRepository;
    private final ReservaRepository reservaRepository;

    public ReservaService(AvaliacaoService avaliacaoService, RotaRepository rotaRepository,
            CaronaRepository caronaRepository, RotaMapperService rotaMapperService, UserRepository userRepository,
            ReservaRepository reservaRepository) {
        this.avaliacaoService = avaliacaoService;
        this.rotaRepository = rotaRepository;
        this.caronaRepository = caronaRepository;
        this.rotaMapperService = rotaMapperService;
        this.userRepository = userRepository;
        this.reservaRepository = reservaRepository;
    }

    public List<BuscaRetornaDto> buscarCaronas(BuscaCaronaDto buscaDto){
        Point saida = geometryFactory.createPoint(
        new Coordinate(buscaDto.getSaidaLon(), buscaDto.getSaidaLat())
      );
        Point destino = geometryFactory.createPoint(
        new Coordinate(buscaDto.getDestinoLon(), buscaDto.getDestinoLat())
      );
        List<BuscaRetornaDto> buscasRetornosDto = new ArrayList<>();
        List<Carona> caronas = caronaRepository.buscarCaronasTodoTrajeto(saida, destino, buscaDto.getData());
        List<Carona> caronasPorParadas = caronaRepository.buscarCaronasPorParadas(saida, destino, buscaDto.getData(), 2000L);
        System.out.println(caronas.size());
        System.out.println(caronasPorParadas.size());

        for (Carona carona : caronasPorParadas) {
            if (!caronas.contains(carona)) {
                caronas.add(carona);
            }
        }
        // buscar na api as distancia das caronas para ver se da 10 km mesmo
            for (Carona carona : caronas) {
                BuscaRetornaDto buscaRetornoDto = new BuscaRetornaDto();
                Point destino02 = carona.getRota().getDestino();
                double destinoLon = destino02.getX();
                double destinoLat = destino02.getY();
                Point saida02 = carona.getRota().getSaida();
                double saidaLon = saida02.getX();
                double saidaLat = saida02.getY();
                buscaRetornoDto.setSaidaLat(saidaLat);
                buscaRetornoDto.setSaidaLon(saidaLon);

                buscaRetornoDto.setDestinoLat(destinoLat);
                buscaRetornoDto.setDestinoLon(destinoLon);
                // buscar o trajeto para exibir no front
                buscaRetornoDto.setDistancia(carona.getRota().getDistancia());
                buscaRetornoDto.setDuracao(carona.getRota().getDuracao());
                buscaRetornoDto.setIdMotorista(carona.getMotorista().getId());
                buscaRetornoDto.setNomeMotorista(carona.getMotorista().getNome());
                buscaRetornoDto.setIdRota(carona.getRota().getId());
                buscaRetornoDto.setData(carona.getData());
                buscaRetornoDto.setHora(carona.getHora());
                List<PontoParadaRetornoDto> paradasDto = new ArrayList<PontoParadaRetornoDto>();
                for(PontoParada parada : carona.getRota().getRota_points()) {
                PontoParadaRetornoDto paradaDto = new PontoParadaRetornoDto();
                paradaDto.setIndexOrder(parada.getIndexOrder());
                paradaDto.setLatPonto(parada.getLocalizacao().getY());
                paradaDto.setLonPonto(parada.getLocalizacao().getX());
                paradaDto.setTextoPonto(parada.getTextoPonto());
                paradasDto.add(paradaDto);
                RankingDto rankingDto = avaliacaoService.calculaRanking(carona.getMotorista());
                buscaRetornoDto.setAvaliacaoMotorista(rankingDto);
                }
                buscaRetornoDto.setParadas(paradasDto);
                buscasRetornosDto.add(buscaRetornoDto);
            }
        return buscasRetornosDto;
    }

    @Transactional
    public ApiResponse cancelarReserva(String username, Long idReserva){
        Optional<User> userOpt = userRepository.findByUsernameAndAtivoTrue(username);
        if(userOpt.isEmpty()){
            return new ApiResponse(false,"Usuário não encontrado ou inativo");
        }
        User user = userOpt.get();
        Optional<Long> idCarona = reservaRepository.buscarIdCarona(idReserva);
        if(idCarona.isEmpty()){
            return new ApiResponse(false,"Reserva não encontrada");
        }
        // Bloqueia primeiro a carona para serializar cancelamentos e o início da viagem.
        Optional<Carona> caronaOpt = caronaRepository.buscarPorIdComBloqueio(idCarona.get());
        if(caronaOpt.isEmpty()){
            return new ApiResponse(false,"Carona não encontrada");
        }
        Carona carona = caronaOpt.get();
        Optional<Reserva> reservaOpt = reservaRepository.buscarPorIdComBloqueio(idReserva);
        if(reservaOpt.isEmpty()){
            return new ApiResponse(false,"Reserva não encontrada");
        }
        Reserva reserva = reservaOpt.get();
        if(!reserva.getPassageiro().getId().equals(user.getId())){
            return new ApiResponse(false,"Você só pode cancelar suas próprias reservas");
        }
        if(reserva.getStatusReserva() == StatusReserva.CANCELADA){
            return new ApiResponse(false,"Esta reserva já foi cancelada");
        }
        if (reserva.getStatusReserva() != StatusReserva.PENDENTE && reserva.getStatusReserva() != StatusReserva.ACEITA) {
            return new ApiResponse(false,"Não é possível cancelar esta reserva no estado atual");
        }
        if(carona.getStatusCarona() != StatusCarona.EM_ESPERA){
            return new ApiResponse(false,"O cancelamento só é permitido enquanto a carona estiver em espera");
        }
        if(carona.getData().isBefore(LocalDate.now())){
            return new ApiResponse(false, "Não é possível cancelar uma reserva de uma carona com data passada");
        }
        if(reserva.getStatusReserva() == StatusReserva.ACEITA){
            carona.setVagasDisponiveis(carona.getVagasDisponiveis() + 1);
            caronaRepository.save(carona);
        }
        reserva.setStatusReserva(StatusReserva.CANCELADA);
        Reserva reservaSalva = reservaRepository.save(reserva);
        if (reservaSalva != null && reservaSalva.getStatusReserva() == StatusReserva.CANCELADA) {
            enviarEmailCancelamentoPassageiro(user, carona);
            enviarEmailCancelamentoMotorista(user, carona);
        }
        return new ApiResponse(true,"Reserva cancelada com sucesso");
    }

    private void enviarEmailCancelamentoPassageiro(User passageiro, Carona carona) {
        String mensagem = """
                Olá, %s!

                Sua reserva foi cancelada com sucesso.

                Saída: %s
                Destino: %s
                Data: %s
                Horário: %s

                Equipe VaiDeBoa
                """.formatted(
                passageiro.getNome(),
                carona.getRota().getSaidaTexto(),
                carona.getRota().getDestinoTexto(),
                carona.getData().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                carona.getHora().format(DateTimeFormatter.ofPattern("HH:mm")));
        try {
            emailService.enviarEmail(mensagem, "Confirmação de cancelamento da reserva", passageiro.getUsername());
        } catch (Exception exception) {
            logger.error("Não foi possível enviar o e-mail de cancelamento para o passageiro", exception);
        }
    }

    private void enviarEmailCancelamentoMotorista(User passageiro, Carona carona) {
        User motorista = carona.getMotorista();
        String mensagem = """
                Olá, %s!

                O passageiro %s cancelou a reserva na sua carona.

                Saída: %s
                Destino: %s
                Data: %s
                Horário: %s

                Sua carona continua ativa. Consulte as reservas atualizadas no aplicativo.

                Equipe VaiDeBoa
                """.formatted(
                motorista.getNome(),
                passageiro.getNome(),
                carona.getRota().getSaidaTexto(),
                carona.getRota().getDestinoTexto(),
                carona.getData().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                carona.getHora().format(DateTimeFormatter.ofPattern("HH:mm")));
        try {
            emailService.enviarEmail(mensagem, "Uma reserva da sua carona foi cancelada", motorista.getUsername());
        } catch (Exception exception) {
            logger.error("Não foi possível enviar o e-mail de cancelamento para o motorista", exception);
        }
    }
}
