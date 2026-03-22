package com.example.vaideboa.service;

import com.example.vaideboa.repository.CaronaRepository;
import com.example.vaideboa.repository.RotaRepository;

import java.util.Optional;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;

import com.example.vaideboa.Dtos.CaronaDto;
import com.example.vaideboa.model.Carona;
import com.example.vaideboa.model.Rota;
import com.example.vaideboa.model.User;
import com.example.vaideboa.repository.UserRepository;

@Service
public class CaronaService {
    private final CaronaRepository caronaRepository;
    private final RotaRepository rotaRepository;
    private final UserRepository userRepository;
    private final GeometryFactory geometryFactory = new GeometryFactory();
    
    
    public CaronaService(CaronaRepository caronaRepository, RotaRepository rotaRepository,
            UserRepository userRepository) {
        this.caronaRepository = caronaRepository;
        this.rotaRepository = rotaRepository;
        this.userRepository = userRepository;
    }


    public boolean cadastrarCarona(CaronaDto caronaDto){
      Optional<User> userOpt = userRepository.findById(caronaDto.getUserId());
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
      Rota rotaSalva = rotaRepository.save(rota);
      if(rotaSalva == null)
      {
        return false;
      }
      carona.setQntAssentos(caronaDto.getQntAssentos());
      carona.setMotorista(user);
      carona.setDataHora(caronaDto.getDataHora());
      carona.setRota(rotaSalva);
      caronaRepository.save(carona);
      return true;
    }
}
