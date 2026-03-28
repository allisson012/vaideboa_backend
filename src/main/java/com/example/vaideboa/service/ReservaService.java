package com.example.vaideboa.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;

import com.example.vaideboa.Dtos.BuscaCaronaDto;
import com.example.vaideboa.Dtos.BuscaRetornaDto;
import com.example.vaideboa.model.Carona;
import com.example.vaideboa.model.Rota;
import com.example.vaideboa.repository.CaronaRepository;
import com.example.vaideboa.repository.RotaRepository;

@Service
public class ReservaService {
    private final RotaRepository rotaRepository;
    private final CaronaRepository caronaRepository;
    private final GeometryFactory geometryFactory = new GeometryFactory();
    
    

    public ReservaService(RotaRepository rotaRepository, CaronaRepository caronaRepository) {
        this.rotaRepository = rotaRepository;
        this.caronaRepository = caronaRepository;
    }



    public List<BuscaRetornaDto> buscarCaronas(BuscaCaronaDto buscaDto){
        Point saida = geometryFactory.createPoint(
        new Coordinate(buscaDto.getSaidaLon(), buscaDto.getSaidaLat())
      );
        Point destino = geometryFactory.createPoint(
        new Coordinate(buscaDto.getDestinoLon(), buscaDto.getDestinoLat())
      );
        List<BuscaRetornaDto> buscasRetornosDto = new ArrayList<>();
        Optional<List<Rota>> rotasOpt = rotaRepository.buscarCaronas(saida,destino);
        List<Rota> rotas = new ArrayList<>();
        Optional<List<Carona>> caronasOpt = Optional.empty();
        List<Carona> caronas = new ArrayList<>();
        if(!rotasOpt.isEmpty()){
            rotas = rotasOpt.get();
        }
            if(!rotas.isEmpty()){
            for (Rota rota : rotas) {
            caronasOpt = caronaRepository.findByRota(rota);
            if (caronasOpt.isPresent()) {
                caronas.addAll(caronasOpt.get());
            }
            }
        
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
                buscaRetornoDto.setIdMotorista(carona.getMotorista().getId());
                buscaRetornoDto.setNomeMotorista(carona.getMotorista().getNome());
                buscaRetornoDto.setIdRota(carona.getRota().getId());
                buscaRetornoDto.setData(carona.getData());
                buscaRetornoDto.setHora(carona.getHora());
                buscasRetornosDto.add(buscaRetornoDto);
            }
    }
        return buscasRetornosDto;
    }
}
