package com.example.vaideboa.service;

import com.example.vaideboa.controller.PedidoController;
import com.example.vaideboa.repository.ReservaRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.vaideboa.Dtos.AgendarCaronaDto;
import com.example.vaideboa.Dtos.ApiResponse;
import com.example.vaideboa.Dtos.PedidoCaronaRetornoDto;
import com.example.vaideboa.model.Carona;
import com.example.vaideboa.model.PedidoCarona;
import com.example.vaideboa.model.Reserva;
import com.example.vaideboa.model.User;
import com.example.vaideboa.model.enums.StatusPedido;
import com.example.vaideboa.repository.CaronaRepository;
import com.example.vaideboa.repository.PedidoCaronaRepository;
import com.example.vaideboa.repository.UserRepository;

@Service
public class PedidoService {

    private final ReservaRepository reservaRepository;
    private final UserRepository userRepository;
    private final CaronaRepository caronaRepository;
    private final PedidoCaronaRepository pedidoCaronaRepository;
    
    public PedidoService(UserRepository userRepository, CaronaRepository caronaRepository,
            PedidoCaronaRepository pedidoCaronaRepository, ReservaRepository reservaRepository) {
        this.userRepository = userRepository;
        this.caronaRepository = caronaRepository;
        this.pedidoCaronaRepository = pedidoCaronaRepository;
        this.reservaRepository = reservaRepository;
    }

    public ApiResponse agendarCarona(AgendarCaronaDto agendarCaronaDto , String username){
      Optional<User> userOpt = userRepository.findByUsernameAndAtivoTrue(username);
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

    public ApiResponse aceitarPedidoCarona(Long id, String username){
      Optional<User> userOpt = userRepository.findByUsernameAndAtivoTrue(username);
      if(userOpt.isEmpty()){
        return new ApiResponse(false, "Usuário não encontrado ou inativo");
      }
      User user = userOpt.get();
      Optional<PedidoCarona> pedidoCaronaOpt = pedidoCaronaRepository.findById(id);
      if(pedidoCaronaOpt.isEmpty()){
        return new ApiResponse(false, "Pedido de carona não encontrado");
      }
      PedidoCarona pedidoCarona = pedidoCaronaOpt.get();
      if(!pedidoCarona.getCarona().getMotorista().equals(user)){
        return new ApiResponse(false, "Apenas o motorista da carona pode aceitar pedidos");
      }
      if(pedidoCarona.getStatus() != StatusPedido.PENDENTE){
        return new ApiResponse(false, "Pedido não pode mais ser alterado");
      }
      pedidoCarona.setStatus(StatusPedido.ACEITO);
      Reserva reserva = new Reserva();
      reserva.setCarona(pedidoCarona.getCarona());
      reserva.setPassageiro(pedidoCarona.getPassageiro());
      reserva.setAprovado(true); // como ainda não tem pagamento estou deixando ele aprovado
      reservaRepository.save(reserva);
      pedidoCaronaRepository.save(pedidoCarona);
      return new ApiResponse(true, "Pedido aceito com sucesso");
    }

    public ApiResponse buscarPedidos(String username){
      Optional<User> userOpt = userRepository.findByUsernameAndAtivoTrue(username);
      if(userOpt.isEmpty()){
        return new ApiResponse(false, "Usuário não encontrado");
      }
      User user = userOpt.get();
      LocalDate data = LocalDate.now(); // data de hoje 
      List<Carona> caronas = caronaRepository.findByMotoristaAndDataGreaterThanEqual(user,data);
      List<PedidoCarona> pedidosCarona = new ArrayList<>();
      for (Carona carona : caronas) {
        List<PedidoCarona> pedidos = pedidoCaronaRepository.findByCaronaAndStatus(carona, StatusPedido.PENDENTE);

        pedidosCarona.addAll(pedidos);
      }
      for (PedidoCarona pedidoCarona : pedidosCarona) {
        PedidoCaronaRetornoDto dto = new PedidoCaronaRetornoDto();
        // dto.setAvaliacao(pedidoCarona.getPassageiro().get);
        dto.setNome(pedidoCarona.getPassageiro().getNome());
        dto.setData(pedidoCarona.getCarona().getData().toString());
        dto.setGenero(pedidoCarona.getPassageiro().getGenero().getDescricao());
        dto.setDistancia(pedidoCarona.getCarona().getRota().getDistancia());
        dto.setDuracao(pedidoCarona.getCarona().getRota().getDuracao());
        dto.setIdPedidoCarona(pedidoCarona.getId());
        dto.setVagasDisponiveis(pedidoCarona.getCarona().getVagasDisponiveis());
        
      }
      
      return null;
    }
}
