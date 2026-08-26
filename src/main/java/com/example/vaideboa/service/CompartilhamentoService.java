package com.example.vaideboa.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.example.vaideboa.Dtos.LocalizacaoDto;
import com.example.vaideboa.model.Carona;
import com.example.vaideboa.model.TrajetoCompartilhado;
import com.example.vaideboa.model.enums.StatusCompartilhamento;
import com.example.vaideboa.repository.CaronaRepository;
import com.example.vaideboa.repository.TrajetoCompartilhadoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CompartilhamentoService {

    private final SimpMessagingTemplate messagingTemplate;
    private final Map<Long, List<Coordinate>> trajetosEmAndamento = new ConcurrentHashMap<>();
    private final CaronaRepository caronaRepository;
    private final TrajetoCompartilhadoRepository trajetoCompartilhadoRepository;
    
    public void atualizarLocalizacao(Long idCarona, double latitude, double longitude) {
        System.out.println("latidute = "+latitude);
        System.out.println("longitude = "+longitude);
        LocalizacaoDto localizacao = new LocalizacaoDto(latitude, longitude);
        // para enviar a localização para outros usuarios
        List<Coordinate> trajeto = trajetosEmAndamento.get(idCarona);
        if(trajeto == null){
            return;
        }

        messagingTemplate.convertAndSend("/topic/carona/" + idCarona, localizacao);

        Coordinate novoPonto = new Coordinate(longitude, latitude);
        if(!trajeto.isEmpty()){
            Coordinate ultimoPonto = trajeto.get(trajeto.size() - 1);
            if (ultimoPonto.equals2D(novoPonto)) {
                return;
            }
        }

        trajeto.add(novoPonto);
        
    }
    public void iniciarCompartilhamento(Long idCarona) {
        TrajetoCompartilhado trajetoCompartilhado = new TrajetoCompartilhado();
        Optional<Carona> caronaOpt = caronaRepository.findById(idCarona);
        if(caronaOpt.isEmpty()){
            return;
        }
        Carona carona = caronaOpt.get();
        Optional<TrajetoCompartilhado> trajetoCompartilhadoOpt = trajetoCompartilhadoRepository.findByCarona(carona);
        if(!trajetoCompartilhadoOpt.isEmpty()){
            return;
        }
        trajetoCompartilhado.setCarona(carona);
        trajetoCompartilhado.setInicio(LocalDateTime.now());
        trajetoCompartilhado.setStatusCompartilhamento(StatusCompartilhamento.EM_ANDAMENTO);
        carona.setTrajetoCompartilhado(trajetoCompartilhado);
        trajetoCompartilhadoRepository.save(trajetoCompartilhado);
        caronaRepository.save(carona);
        trajetosEmAndamento.put(idCarona, new ArrayList<>());
    }

    public void removerCompartilhamento(Long idCarona){
        Optional<Carona> caronaOpt = caronaRepository.findById(idCarona);
        if(caronaOpt.isEmpty()){
            return;
        }
        Carona carona = caronaOpt.get();
        Optional<TrajetoCompartilhado> trajetoCompartilhadoOpt = trajetoCompartilhadoRepository.findByCarona(carona);
        if(trajetoCompartilhadoOpt.isEmpty()){
            return;
        }
        TrajetoCompartilhado trajetoCompartilhado = trajetoCompartilhadoOpt.get();
        trajetoCompartilhado.setStatusCompartilhamento(StatusCompartilhamento.CANCELADO);
        trajetoCompartilhado.setFim(LocalDateTime.now());
        trajetoCompartilhadoRepository.save(trajetoCompartilhado);
        // estou perdendo os pontos do caminho feito ate o momento pensar se quero salvar ou não
        trajetosEmAndamento.remove(idCarona);
    }

    public void finalizarCompartilhamento(Long idCarona){
        List<Coordinate> trajeto = trajetosEmAndamento.get(idCarona);
        System.out.println("Quantidade de pontos: " + trajeto.size());
        Optional<Carona> caronaOpt = caronaRepository.findById(idCarona);
        if(caronaOpt.isEmpty()){
            return;
        }
        Carona carona = caronaOpt.get();
        Optional<TrajetoCompartilhado> trajetoCompartilhadoOpt = trajetoCompartilhadoRepository.findByCarona(carona);
        if(trajetoCompartilhadoOpt.isEmpty()){
            return;
        }
        TrajetoCompartilhado trajetoCompartilhado = trajetoCompartilhadoOpt.get();
        trajetoCompartilhado.setFim(LocalDateTime.now());
        trajetoCompartilhado.setStatusCompartilhamento(StatusCompartilhamento.FINALIZADO);
        if (trajeto != null && trajeto.size() >= 2) {
            trajetoCompartilhado.setDistancia_percorrida(calcularDistancia(trajeto));
            trajetoCompartilhado.setTrajeto(criarLineString(trajeto));
        }

        trajetoCompartilhadoRepository.save(trajetoCompartilhado);
        trajetosEmAndamento.remove(idCarona);
    }

    public LineString criarLineString(List<Coordinate> trajeto){
        if(trajeto == null || trajeto.size() < 2){
            return null;
        }
        GeometryFactory geometryFactory = new GeometryFactory();
        Coordinate[] coordenadas = trajeto.toArray(new Coordinate[0]);
        LineString lineString = geometryFactory.createLineString(coordenadas);
        lineString.setSRID(4326);
        return lineString;
    }

    public double calcularDistancia(List<Coordinate> trajeto) {
        double distanciaTotal = 0;
        for (int i = 1; i < trajeto.size(); i++) {
            Coordinate pontoAnterior = trajeto.get(i - 1);
            Coordinate pontoAtual = trajeto.get(i);
            distanciaTotal += distanciaEntrePontos(pontoAnterior,pontoAtual);
        }
        return distanciaTotal;
    }

    private double distanciaEntrePontos(Coordinate p1,Coordinate p2) {
        double raioTerra = 6371000; 
        double lat1 = Math.toRadians(p1.getY());
        double lat2 = Math.toRadians(p2.getY());
        double deltaLat = Math.toRadians(p2.getY() - p1.getY());
        double deltaLon = Math.toRadians(p2.getX() - p1.getX());
        double a =Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +Math.cos(lat1)* Math.cos(lat2)* Math.sin(deltaLon / 2)* Math.sin(deltaLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a),Math.sqrt(1 - a));
        return raioTerra * c;
    }
}