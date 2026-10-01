package com.example.vaideboa.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.example.vaideboa.Dtos.LocalizacaoDto;
import com.example.vaideboa.model.Carona;
import com.example.vaideboa.model.TrajetoCompartilhado;
import com.example.vaideboa.model.enums.StatusCarona;
import com.example.vaideboa.model.enums.StatusCompartilhamento;
import com.example.vaideboa.repository.CaronaRepository;
import com.example.vaideboa.repository.TrajetoCompartilhadoRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompartilhamentoService {

    private final SimpMessagingTemplate messagingTemplate;
    private final Map<Long, List<Coordinate>> trajetosEmAndamento = new ConcurrentHashMap<>();
    private final CaronaRepository caronaRepository;
    private final TrajetoCompartilhadoRepository trajetoCompartilhadoRepository;
    
    public void atualizarLocalizacao(Long idCarona, double latitude, double longitude, String username) {
        Optional<Carona> caronaOpt = caronaRepository.findById(idCarona);
        if (caronaOpt.isEmpty()) {
            log.warn("Localização ignorada: carona {} não encontrada. Motorista: {}", idCarona, username);
            return;
        }

        Carona carona = caronaOpt.get();
        if (!StatusCarona.EM_ANDAMENTO.equals(carona.getStatusCarona())) {
            log.warn("Localização ignorada: carona {} não está em andamento. Status: {}", idCarona, carona.getStatusCarona());
            return;
        }

        if (!carona.getMotorista().getUsername().equals(username)) {
            log.warn("Localização ignorada: usuário {} não é o motorista da carona {}", username, idCarona);
            return;
        }

        List<Coordinate> trajeto = trajetosEmAndamento.computeIfAbsent(idCarona,
                ignored -> restaurarTrajeto(carona));
        if (trajeto == null) {
            log.warn("Localização ignorada: não há compartilhamento ativo para a carona {}", idCarona);
            return;
        }

        log.info("Atualizando localização da carona {}: latitude={}, longitude={}", idCarona, latitude, longitude);
        LocalizacaoDto localizacao = new LocalizacaoDto(latitude, longitude);
        messagingTemplate.convertAndSend("/topic/carona/" + idCarona, localizacao);

        Coordinate novoPonto = new Coordinate(longitude, latitude);
        if(!trajeto.isEmpty()){
            Coordinate ultimoPonto = trajeto.get(trajeto.size() - 1);
            if (ultimoPonto.equals2D(novoPonto)) {
                return;
            }
        }

        trajeto.add(novoPonto);
        salvarTrajeto(trajeto, carona);
    }

    public void iniciarCompartilhamento(Long idCarona) {
        Optional<Carona> caronaOpt = caronaRepository.findById(idCarona);
        if(caronaOpt.isEmpty()){
            return;
        }
        Carona carona = caronaOpt.get();
        Optional<TrajetoCompartilhado> trajetoCompartilhadoOpt = trajetoCompartilhadoRepository.findByCarona(carona);
        if (trajetoCompartilhadoOpt.isPresent()) {
            TrajetoCompartilhado trajetoCompartilhado = trajetoCompartilhadoOpt.get();
            if (StatusCompartilhamento.EM_ANDAMENTO.equals(trajetoCompartilhado.getStatusCompartilhamento())) {
                trajetosEmAndamento.putIfAbsent(idCarona, restaurarTrajeto(trajetoCompartilhado));
            }
            return;
        }
        TrajetoCompartilhado trajetoCompartilhado = new TrajetoCompartilhado();
        trajetoCompartilhado.setCarona(carona);
        trajetoCompartilhado.setInicio(LocalDateTime.now());
        trajetoCompartilhado.setStatusCompartilhamento(StatusCompartilhamento.EM_ANDAMENTO);
        carona.setTrajetoCompartilhado(trajetoCompartilhado);
        trajetoCompartilhadoRepository.save(trajetoCompartilhado);
        caronaRepository.save(carona);
        trajetosEmAndamento.put(idCarona, new CopyOnWriteArrayList<>());
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
        Optional<Carona> caronaOpt = caronaRepository.findById(idCarona);
        if(caronaOpt.isEmpty()){
            return;
        }
        Carona carona = caronaOpt.get();
        List<Coordinate> trajeto = trajetosEmAndamento.computeIfAbsent(idCarona,
                ignored -> restaurarTrajeto(carona));
        log.info("Finalizando compartilhamento da carona {} com {} pontos", idCarona, trajeto == null ? 0 : trajeto.size());
        Optional<TrajetoCompartilhado> trajetoCompartilhadoOpt = trajetoCompartilhadoRepository.findByCarona(carona);
        if(trajetoCompartilhadoOpt.isEmpty()){
            return;
        }
        TrajetoCompartilhado trajetoCompartilhado = trajetoCompartilhadoOpt.get();
        trajetoCompartilhado.setFim(LocalDateTime.now());
        trajetoCompartilhado.setStatusCompartilhamento(StatusCompartilhamento.FINALIZADO);
        if (trajeto != null && !trajeto.isEmpty()) {
            trajetoCompartilhado.setTrajeto(criarLineString(trajeto));
            trajetoCompartilhado.setDistancia_percorrida(calcularDistancia(trajeto));
        }

        trajetoCompartilhadoRepository.save(trajetoCompartilhado);
        trajetosEmAndamento.remove(idCarona);
    }

    public LineString criarLineString(List<Coordinate> trajeto){
        if(trajeto == null || trajeto.isEmpty()){
            return null;
        }
        GeometryFactory geometryFactory = new GeometryFactory();
        Coordinate[] coordenadas = trajeto.size() == 1
                ? new Coordinate[] { trajeto.getFirst(), trajeto.getFirst() }
                : trajeto.toArray(new Coordinate[0]);
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
    public List<LocalizacaoDto> obterTrajetoAtual(Long idCarona) {
        Optional<Carona> caronaOpt = caronaRepository.findById(idCarona);
        if (caronaOpt.isEmpty()) {
            return List.of();
        }
        return restaurarTrajeto(caronaOpt.get()).stream()
                .map(coordenada -> new LocalizacaoDto(coordenada.getY(), coordenada.getX()))
                .toList();
    }

    private List<Coordinate> restaurarTrajeto(Carona carona) {
        return trajetoCompartilhadoRepository.findByCarona(carona)
                .filter(trajeto -> StatusCompartilhamento.EM_ANDAMENTO.equals(trajeto.getStatusCompartilhamento()))
                .map(this::restaurarTrajeto)
                .orElse(null);
    }

    private List<Coordinate> restaurarTrajeto(TrajetoCompartilhado trajetoCompartilhado) {
        List<Coordinate> coordenadas = new CopyOnWriteArrayList<>();
        LineString linha = trajetoCompartilhado.getTrajeto();
        if (linha == null) {
            return coordenadas;
        }

        for (Coordinate coordenada : linha.getCoordinates()) {
            if (coordenadas.isEmpty() || !coordenadas.getLast().equals2D(coordenada)) {
                coordenadas.add(new Coordinate(coordenada));
            }
        }
        return coordenadas;
    }

    private void salvarTrajeto(List<Coordinate> trajetos, Carona carona) {
        trajetoCompartilhadoRepository.findByCarona(carona).ifPresent(trajetoCompartilhado -> {
            trajetoCompartilhado.setTrajeto(criarLineString(trajetos));
            trajetoCompartilhado.setDistancia_percorrida(calcularDistancia(trajetos));
            trajetoCompartilhadoRepository.save(trajetoCompartilhado);
        });
    }
}
