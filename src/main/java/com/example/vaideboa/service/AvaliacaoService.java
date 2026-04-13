package com.example.vaideboa.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.vaideboa.Dtos.ApiResponse;
import com.example.vaideboa.Dtos.AvaliacaoDto;
import com.example.vaideboa.model.Avaliacao;
import com.example.vaideboa.model.Carona;
import com.example.vaideboa.model.Reserva;
import com.example.vaideboa.model.User;
import com.example.vaideboa.model.enums.TipoAvaliacao;
import com.example.vaideboa.repository.AvaliacaoRepository;
import com.example.vaideboa.repository.CaronaRepository;
import com.example.vaideboa.repository.ReservaRepository;
import com.example.vaideboa.repository.UserRepository;

@Service
public class AvaliacaoService {
private final UserRepository userRepository;
private final AvaliacaoRepository avaliacaoRepository;
private final CaronaRepository caronaRepository;
private final ReservaRepository reservaRepository;

    public AvaliacaoService(UserRepository userRepository, AvaliacaoRepository avaliacaoRepository,
        CaronaRepository caronaRepository, ReservaRepository reservaRepository) {
    this.userRepository = userRepository;
    this.avaliacaoRepository = avaliacaoRepository;
    this.caronaRepository = caronaRepository;
    this.reservaRepository = reservaRepository;
}

    @Transactional
    public boolean criarAvaliacoes(Carona carona){
        
        List<Reserva> reservas = carona.getReservas();
        if(reservas == null || reservas.isEmpty()){
            return false;
        }

        for (Reserva reserva : reservas) {

            User motorista = carona.getMotorista();
            User passageiro = reserva.getPassageiro();

            // motorista avalia passageiro
            Avaliacao a1 = new Avaliacao();
            a1.setReserva(reserva);
            a1.setAvaliador(motorista);
            a1.setAvaliado(passageiro);
            a1.setTipo(TipoAvaliacao.MOTORISTA_AVALIA_PASSAGEIRO);
            a1.setNota(null);

            avaliacaoRepository.save(a1);

            // passageiro avalia motorista
            Avaliacao a2 = new Avaliacao();
            a2.setReserva(reserva);
            a2.setAvaliador(passageiro);
            a2.setAvaliado(motorista);
            a2.setTipo(TipoAvaliacao.PASSAGEIRO_AVALIA_MOTORISTA);
            a2.setNota(null);

            avaliacaoRepository.save(a2);
        }
        return true;
    }

    public ApiResponse cadastrarAvaliacao(AvaliacaoDto avaliacaoDto, String username){
        Optional<User> userOpt = userRepository.findByUsernameAndAtivoTrue(username);
        if(userOpt.isEmpty()){
          return new ApiResponse(false, "Usuário não encontrado"); 
        }
        User user = userOpt.get();
        Optional<Reserva> reservaOpt = reservaRepository.findById(avaliacaoDto.getIdReserva());
        if(reservaOpt.isEmpty()){
            return new ApiResponse(false, "Carona não encontrada");
        }
        Reserva reserva = reservaOpt.get();

        boolean isMotorista = reserva.getCarona().getMotorista().equals(user);
        boolean isPassageiro = reserva.getPassageiro().equals(user);
        if(!isMotorista && !isPassageiro){
            return new ApiResponse(false, "Usuário não pertence a esta reserva");
        }

        List<Avaliacao> avaliacoes = reserva.getAvaliacoes();
        Avaliacao avaliacaoSalva = null;
        
        if(isMotorista){
          for (Avaliacao avaliacao : avaliacoes) {
            if(avaliacao.getTipo() == TipoAvaliacao.MOTORISTA_AVALIA_PASSAGEIRO){
                avaliacaoSalva = avaliacao;
                break;
            }
          }
        }
        // pega do usuario
        if(isPassageiro){
          for (Avaliacao avaliacao : avaliacoes) {
            if(avaliacao.getTipo() == TipoAvaliacao.PASSAGEIRO_AVALIA_MOTORISTA){
                avaliacaoSalva = avaliacao;
                break;
            }
          }
        }

        if(avaliacaoSalva == null){
        return new ApiResponse(false, "Avaliação não encontrada para este usuário");
        }


        avaliacaoSalva.setNota(avaliacaoDto.getEstrela());
        avaliacaoSalva.setComentario(avaliacaoDto.getMensagem());
        avaliacaoRepository.save(avaliacaoSalva);

        return new ApiResponse(true, "Avaliação salva com sucesso");
    }
}
