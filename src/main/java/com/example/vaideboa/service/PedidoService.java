package com.example.vaideboa.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.vaideboa.Dtos.AgendarCaronaDto;
import com.example.vaideboa.Dtos.ApiResponse;
import com.example.vaideboa.model.Carona;
import com.example.vaideboa.model.PedidoCarona;
import com.example.vaideboa.model.User;
import com.example.vaideboa.model.enums.StatusPedido;
import com.example.vaideboa.repository.CaronaRepository;
import com.example.vaideboa.repository.PedidoCaronaRepository;
import com.example.vaideboa.repository.UserRepository;

@Service
public class PedidoService {
    private final UserRepository userRepository;
    private final CaronaRepository caronaRepository;
    private final PedidoCaronaRepository pedidoCaronaRepository;
    
    public PedidoService(UserRepository userRepository, CaronaRepository caronaRepository,
            PedidoCaronaRepository pedidoCaronaRepository) {
        this.userRepository = userRepository;
        this.caronaRepository = caronaRepository;
        this.pedidoCaronaRepository = pedidoCaronaRepository;
    }

    public ApiResponse agendarCarona(AgendarCaronaDto agendarCaronaDto , String username){
      Optional<User> userOpt = userRepository.findByUsername(username);
      if(userOpt.isEmpty()){
        return new ApiResponse(false, "Usuário não encontrado");
      }
      User user = userOpt.get();
      Optional<Carona> caronaOpt = caronaRepository.findById(agendarCaronaDto.getIdCarona());
      if(caronaOpt.isEmpty()){
        return new ApiResponse(false, "Carona não encontrada");
      }
      Carona carona = caronaOpt.get();
      if(user.equals(carona.getMotorista())){
        // motorista é igual ao usuario que esta agendando retornar um erro do tipo
        return new ApiResponse(false, "Motorista não pode agendar própria carona");
      }
      if(pedidoCaronaRepository.existsByPassageiroAndCarona(user, carona)){
        return new ApiResponse(false, "Pedido já existe");
      }
      if(carona.getVagasDisponiveis() == 0){
        return new ApiResponse(false, "Carona lotada");
      }
      PedidoCarona pedidoCarona = new PedidoCarona();
      pedidoCarona.setCarona(carona);
      pedidoCarona.setPassageiro(user);
      pedidoCarona.setStatus(StatusPedido.PENDENTE);
      pedidoCaronaRepository.save(pedidoCarona);

      return new ApiResponse(true, "Pedido agendado com sucesso");
    }
}
