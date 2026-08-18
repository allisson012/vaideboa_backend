package com.example.vaideboa.service;

import com.example.vaideboa.Dtos.AgendarCaronaDto;
import com.example.vaideboa.Dtos.ApiResponse;
import com.example.vaideboa.Dtos.PedidoCaronaRetornoDto;
import com.example.vaideboa.Dtos.PontoParadaRetornoDto;
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

import com.example.vaideboa.model.Avaliacao;
import com.example.vaideboa.model.Carona;
import com.example.vaideboa.model.PedidoCarona;
import com.example.vaideboa.model.PontoParada;
import com.example.vaideboa.model.Reserva;
import com.example.vaideboa.model.User;
import com.example.vaideboa.model.enums.StatusPedido;
import com.example.vaideboa.model.enums.TipoAvaliacao;
import com.example.vaideboa.repository.AvaliacaoRepository;
import com.example.vaideboa.repository.CaronaRepository;
import com.example.vaideboa.repository.PedidoCaronaRepository;
import com.example.vaideboa.repository.UserRepository;

@Service
public class PedidoService {

    private final ReservaRepository reservaRepository;
    private final UserRepository userRepository;
    private final CaronaRepository caronaRepository;
    private final PedidoCaronaRepository pedidoCaronaRepository;
    private final AvaliacaoRepository avaliacaoRepository;
    private final GeometryFactory geometryFactory = new GeometryFactory();
    
    public PedidoService(UserRepository userRepository, CaronaRepository caronaRepository,
            PedidoCaronaRepository pedidoCaronaRepository, ReservaRepository reservaRepository,
            AvaliacaoRepository avaliacaoRepository) {
        this.userRepository = userRepository;
        this.caronaRepository = caronaRepository;
        this.pedidoCaronaRepository = pedidoCaronaRepository;
        this.reservaRepository = reservaRepository;
        this.avaliacaoRepository = avaliacaoRepository;
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
      if(pedidoCarona.getCarona().getVagasDisponiveis() == 0){
        return new ApiResponse(false, "Veiculo não tem vagas disponiveis");
      }
      pedidoCarona.setStatus(StatusPedido.ACEITO);
      Carona carona = pedidoCarona.getCarona();
      Reserva reserva = new Reserva();
      reserva.setSaida(pedidoCarona.getSaida());
      reserva.setDestino(pedidoCarona.getDestino());
      reserva.setCarona(pedidoCarona.getCarona());
      reserva.setPassageiro(pedidoCarona.getPassageiro());
      reserva.setAprovado(true); // como ainda não tem pagamento estou deixando ele aprovado
      carona.setVagasDisponiveis(carona.getVagasDisponiveis() - 1);
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
        List<PontoParadaRetornoDto> paradasDto = new ArrayList<PontoParadaRetornoDto>();
        for(PontoParada parada : pedidoCarona.getCarona().getRota().getRota_points()) {
        PontoParadaRetornoDto paradaDto = new PontoParadaRetornoDto();
        paradaDto.setIndexOrder(parada.getIndexOrder());
        paradaDto.setLatPonto(parada.getLocalizacao().getY());
        paradaDto.setLonPonto(parada.getLocalizacao().getX());
        paradaDto.setTextoPonto(parada.getTextoPonto());
        paradasDto.add(paradaDto);
        }
        dto.setParadas(paradasDto);

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
        List<PontoParadaRetornoDto> paradasDto = new ArrayList<PontoParadaRetornoDto>();
        for(PontoParada parada : pedidoCarona.getCarona().getRota().getRota_points()) {
        PontoParadaRetornoDto paradaDto = new PontoParadaRetornoDto();
        paradaDto.setIndexOrder(parada.getIndexOrder());
        paradaDto.setLatPonto(parada.getLocalizacao().getY());
        paradaDto.setLonPonto(parada.getLocalizacao().getX());
        paradaDto.setTextoPonto(parada.getTextoPonto());
        paradasDto.add(paradaDto);
        }
        dto.setParadas(paradasDto);

        if (pedidoCarona.getStatus() == StatusPedido.ACEITO) {
          Optional<Reserva> reservaOpt = reservaRepository.findByCaronaAndPassageiro(
              pedidoCarona.getCarona(), pedidoCarona.getPassageiro());
          if (reservaOpt.isPresent()) {
            dto.setIdReserva(reservaOpt.get().getId());
            Optional<Avaliacao> avaliacaoOpt = avaliacaoRepository.findByReservaAndAvaliadoAndAvaliador(
                reservaOpt.get(), pedidoCarona.getPassageiro(), user);
            if (avaliacaoOpt.isPresent()) {
              dto.setIdAvaliacao(avaliacaoOpt.get().getId());
            }
          }
        }


        dtos.add(dto);
      }
      return new ApiResponse(true, "Pedidos buscados com sucesso", dtos);
    }

    public ApiResponse recusarPedido(String username, Long id){
      Optional<User> userOpt = userRepository.findByUsernameAndAtivoTrue(username);
      if(userOpt.isEmpty()){
        return new ApiResponse(false,"Usuário não encontrado");
      }
      User user = userOpt.get();
      Optional<PedidoCarona> pedidoCaronaOpt = pedidoCaronaRepository.findById(id);
      if(pedidoCaronaOpt.isEmpty()){
        return new ApiResponse(false,"Pedido de Carona não encontrado");
      }
      PedidoCarona pedidoCarona = pedidoCaronaOpt.get();
      if(!pedidoCarona.getCarona().getMotorista().equals(user)){
        return new ApiResponse(false,"Você não tem permissão para modificar este pedido de carona.");
      }
      if (pedidoCarona.getStatus() == StatusPedido.RECUSADO) {
        return new ApiResponse(false,"Este pedido já foi recusado.");
      }
      pedidoCarona.setStatus(StatusPedido.RECUSADO);
      pedidoCaronaRepository.save(pedidoCarona);
      return new ApiResponse(true,"Pedido recusado com sucesso");
    }
}
