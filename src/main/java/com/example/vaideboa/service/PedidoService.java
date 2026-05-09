package com.example.vaideboa.service;

import com.example.vaideboa.controller.PedidoController;
import com.example.vaideboa.repository.ReservaRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
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
    private final GeometryFactory geometryFactory = new GeometryFactory();
    
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
      Point saida = geometryFactory.createPoint(
        new Coordinate(agendarCaronaDto.getSaidaLng(), agendarCaronaDto.getSaidaLat())
      );
      Point destino = geometryFactory.createPoint(
        new Coordinate(agendarCaronaDto.getDestinoLng(), agendarCaronaDto.getDestinoLat())
      );
      PedidoCarona pedidoCarona = new PedidoCarona();
      pedidoCarona.setCarona(carona);
      pedidoCarona.setPassageiro(user);
      pedidoCarona.setStatus(StatusPedido.PENDENTE);
      pedidoCarona.setSaida(saida);
      pedidoCarona.setDestino(destino);
      LocalDate dataPedido = LocalDate.now();
      pedidoCarona.setDataPedido(dataPedido);
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
      reserva.setSaida(pedidoCarona.getSaida());
      reserva.setDestino(pedidoCarona.getDestino());
      reserva.setCarona(pedidoCarona.getCarona());
      reserva.setPassageiro(pedidoCarona.getPassageiro());
      reserva.setAprovado(true); // como ainda não tem pagamento estou deixando ele aprovado
      // tenho que tirar um na vagas disponiveis da Carona
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
      List<PedidoCaronaRetornoDto> dtos = new ArrayList<>();
      for (PedidoCarona pedidoCarona : pedidosCarona) {
        PedidoCaronaRetornoDto dto = new PedidoCaronaRetornoDto();
        dto.setNome(pedidoCarona.getPassageiro().getNome());
        dto.setFoto(pedidoCarona.getPassageiro().getFoto());
        dto.setData(pedidoCarona.getCarona().getData().toString());
        dto.setGenero(pedidoCarona.getPassageiro().getGenero().getDescricao());
        dto.setDistancia(pedidoCarona.getCarona().getRota().getDistancia());
        dto.setDuracao(pedidoCarona.getCarona().getRota().getDuracao());
        dto.setIdPedidoCarona(pedidoCarona.getId());
        dto.setVagasDisponiveis(pedidoCarona.getCarona().getVagasDisponiveis());
        dto.setLatSaida(pedidoCarona.getCarona().getRota().getSaida().getY());
        dto.setLonSaida(pedidoCarona.getCarona().getRota().getSaida().getX());
        dto.setSaidaTexto(pedidoCarona.getCarona().getRota().getSaidaTexto());

        dto.setLatDestino(pedidoCarona.getCarona().getRota().getDestino().getY());
        dto.setLonDestino(pedidoCarona.getCarona().getRota().getDestino().getX());
        dto.setDestinoTexto(pedidoCarona.getCarona().getRota().getDestinoTexto());

        dtos.add(dto);
      }
      
      return new ApiResponse(true, "Pedidos pegos com sucesso", dtos);
    }

    public ApiResponse buscarTodosPedidos(Long idCarona, String username){
      Optional<User> userOpt = userRepository.findByUsernameAndAtivoTrue(username);
      if(userOpt.isEmpty()){
        return new ApiResponse(false, "Usuário não encontado");
      }
      User user = userOpt.get();
      Optional<Carona> caronaOpt = caronaRepository.findById(idCarona);
      if(caronaOpt.isEmpty()){
        return new ApiResponse(false, "Carona não encontrada");
      }
      Carona carona = caronaOpt.get();
      if(!carona.getMotorista().equals(user)){
        return new ApiResponse(false, "Usuário não tem acesso a carona");
      }
      List<PedidoCarona> pedidosCarona = pedidoCaronaRepository.findByCaronaId(idCarona);
      if(pedidosCarona.isEmpty()){
        return new ApiResponse(false,"Carona não possui nenhum pedido");
      }
      List<PedidoCaronaRetornoDto> dtos = new ArrayList<>();
      for (PedidoCarona pedidoCarona : pedidosCarona) {
        PedidoCaronaRetornoDto dto = new PedidoCaronaRetornoDto();
        dto.setIdUser(pedidoCarona.getPassageiro().getId());
      //  dto.setAvaliacao(pedidoCarona.getPassageiro().getNota());
        dto.setData(pedidoCarona.getCarona().getData().toString());
        dto.setHora(pedidoCarona.getCarona().getHora().toString());
        dto.setFoto(pedidoCarona.getPassageiro().getFoto());
        dto.setGenero(pedidoCarona.getPassageiro().getGenero().toString());
        dto.setStatusPedido(pedidoCarona.getStatus().toString());
        dto.setNome(pedidoCarona.getPassageiro().getNome());
        dto.setVagasDisponiveis(pedidoCarona.getCarona().getVagasDisponiveis());
        dto.setIdPedidoCarona(pedidoCarona.getId());
        dto.setLatSaida(pedidoCarona.getCarona().getRota().getSaida().getY());
        dto.setLonSaida(pedidoCarona.getCarona().getRota().getSaida().getX());
        dto.setSaidaTexto(pedidoCarona.getCarona().getRota().getSaidaTexto());

        dto.setLatDestino(pedidoCarona.getCarona().getRota().getDestino().getY());
        dto.setLonDestino(pedidoCarona.getCarona().getRota().getDestino().getX());
        dto.setDestinoTexto(pedidoCarona.getCarona().getRota().getDestinoTexto());
        dto.setDuracao(pedidoCarona.getCarona().getRota().getDuracao());
        dto.setIdCarona(pedidoCarona.getCarona().getId());
        dto.setDistancia(pedidoCarona.getCarona().getRota().getDistancia());
        dto.setDataPedido(pedidoCarona.getDataPedido() != null ? pedidoCarona.getDataPedido().toString() : "");
        dtos.add(dto);
      }
      return new ApiResponse(true, "Pedidos buscados com sucesso", dtos);
    }
}
