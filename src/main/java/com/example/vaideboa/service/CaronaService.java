package com.example.vaideboa.service;

import com.example.vaideboa.repository.CaronaRepository;
import com.example.vaideboa.repository.RotaRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;

import com.example.vaideboa.Dtos.ApiResponse;
import com.example.vaideboa.Dtos.CaronaDto;
import com.example.vaideboa.Dtos.RotaInfoDto;
import com.example.vaideboa.Dtos.ViagemRealizadaDTO;
import com.example.vaideboa.model.Carona;
import com.example.vaideboa.model.Reserva;
import com.example.vaideboa.model.Rota;
import com.example.vaideboa.model.User;
import com.example.vaideboa.repository.UserRepository;

@Service
public class CaronaService {
    private final CaronaRepository caronaRepository;
    private final RotaRepository rotaRepository;
    private final UserRepository userRepository; 
    private final RotaService rotaService;
    private final AvaliacaoService avaliacaoService;
    private final GeometryFactory geometryFactory = new GeometryFactory();
    

    public CaronaService(CaronaRepository caronaRepository, RotaRepository rotaRepository,
            UserRepository userRepository, RotaService rotaService, AvaliacaoService avaliacaoService) {
        this.caronaRepository = caronaRepository;
        this.rotaRepository = rotaRepository;
        this.userRepository = userRepository;
        this.rotaService = rotaService;
        this.avaliacaoService = avaliacaoService;
    }

    public boolean cadastrarCarona(CaronaDto caronaDto , String username){
      Optional<User> userOpt = userRepository.findByUsernameAndAtivoTrue(username);
      if(userOpt.isEmpty()){
        return false;
      }
      User user = userOpt.get();
      Rota rota = new Rota();
      Carona carona = new Carona();
      Point saida = geometryFactory.createPoint(
        new Coordinate(caronaDto.getSaidaLng(), caronaDto.getSaidaLat())
      );
      Point destino = geometryFactory.createPoint(
        new Coordinate(caronaDto.getDestinoLng(), caronaDto.getDestinoLat())
      );
      rota.setSaida(saida);
      rota.setDestino(destino);
      String geojson = rotaService.getRota(saida, destino);
      LineString trajeto = rotaService.salvarRota(geojson);
      rota.setTrajeto(trajeto);
      RotaInfoDto rotaInfoDto = rotaService.extrairInfoRota(geojson);
      rota.setDistancia(rotaInfoDto.getDistanciaKm());
      rota.setDuracao(rotaInfoDto.getDuracaoMin());
      Rota rotaSalva = rotaRepository.save(rota);
      if(rotaSalva == null)
      {
        return false;
      }
      carona.setQntAssentos(caronaDto.getQntAssentos());
      carona.setVagasDisponiveis(caronaDto.getQntAssentos());
      carona.setMotorista(user);
      carona.setData(caronaDto.getData());
      carona.setHora(caronaDto.getHora());
      carona.setRota(rotaSalva);
      caronaRepository.save(carona);
      return true;
    }

    public ApiResponse finalizarCorrida(Long idCarona, String username){
      Optional<User> userOpt = userRepository.findByUsernameAndAtivoTrue(username);
      if(userOpt.isEmpty()){
        return new ApiResponse(false, "Usuário não encontrado");
      }
      Optional<Carona> caronaOpt = caronaRepository.findById(idCarona);
      if(caronaOpt.isEmpty()){
        return new ApiResponse(false, "Carona não encontrada");
      }
      Carona carona = caronaOpt.get();
      User user = userOpt.get();
      if(carona.isRealizado()){
        return new ApiResponse(false, "Carona já realizada");
      }
      if(!carona.getMotorista().equals(user)){
        return new ApiResponse(false,"Usuário não tem acesso a carona pois não é o motorista");
      }
      carona.setRealizado(true);
      boolean sucesso = avaliacaoService.criarAvaliacoes(carona);
      if(!sucesso){
          return new ApiResponse(false, "Erro ao criar avaliações");
      }
      caronaRepository.save(carona); 
      return new ApiResponse(true, "Carona finalizada com sucesso");
    }

    public List<ViagemRealizadaDTO> minhasViagens(String username) {
    Optional<User> userOpt = userRepository.findByUsernameAndAtivoTrue(username);
    if (userOpt.isEmpty()) {
      return new ArrayList<>();
    }
    User user = userOpt.get();
    List<ViagemRealizadaDTO> resultado = new ArrayList<>();
    for (Carona c : user.getMinhasCaronas()) {
      Point origem = c.getRota().getSaida();
      Point destino = c.getRota().getDestino();
      resultado.add(new ViagemRealizadaDTO(
        c.getId(),
        origem.getY(), 
        origem.getX(), 
        destino.getY(),
        destino.getX(),
        c.getData(),
        c.getHora(),
        "MOTORISTA"
      ));
    }
    for (Reserva r : user.getMinhasReservas()) {
      Carona c = r.getCarona();
      Point origem = c.getRota().getSaida();
      Point destino = c.getRota().getDestino();
      resultado.add(new ViagemRealizadaDTO(
        c.getId(),
        origem.getY(),
        origem.getX(),
        destino.getY(),
        destino.getX(),
        c.getData(),
        c.getHora(),
        "PASSAGEIRO"
      ));
    }
    resultado.sort(Comparator
      .comparing(ViagemRealizadaDTO::getData)
      .thenComparing(ViagemRealizadaDTO::getHora));
    return resultado;
  }

}
