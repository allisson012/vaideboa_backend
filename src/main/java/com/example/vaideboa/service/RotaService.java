package com.example.vaideboa.service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.vaideboa.model.Rota;
import com.example.vaideboa.repository.RotaRepository;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;

@Service
public class RotaService {

    @Value("${api.key}")
    private String apiKey;
    private final RotaMapperService rotaMapperService;
    private final RotaRepository rotaRepository;

    public RotaService(RotaMapperService rotaMapperService, RotaRepository rotaRepository) {
        this.rotaMapperService = rotaMapperService;
        this.rotaRepository = rotaRepository;
    }

    public String getRota(Point saida, Point destino) {

        String url = "https://api.openrouteservice.org/v2/directions/driving-car/geojson";

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("coordinates", Arrays.asList(
                Arrays.asList(saida.getX(), saida.getY()),
                Arrays.asList(destino.getX(), destino.getY())
        ));

        HttpEntity<Map<String, Object>> request =
                new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        // aqui eu posso enviar para meu banco como uma lineString
        return response.getBody();
    }

    public String calcularDistancia(Point saida, Point destino){
        String url = "https://api.openrouteservice.org/v2/directions/driving-car/geojson";
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Authorization", "Bearer " + apiKey);
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("coordinates", Arrays.asList(
               Arrays.asList(saida.getX(), saida.getY()),
               Arrays.asList(destino.getX(), destino.getY()) 
        ));
        
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, httpHeaders);

        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        if(response.getBody().isEmpty()){
           return "Erro ao calcular distancia";
        }
        String responseString = response.getBody();

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(responseString);

        double distancia = root
        .path("features").get(0)
        .path("properties")
        .path("segments").get(0)
        .path("distance")
        .asDouble();

        double kms = distancia / 1000;
        kms = Math.round(kms * 100.0);
        String kmsFormatado = String.format("%.2f", distancia / 1000.0).replace(".", ",");

        return kmsFormatado;
    }   

    public LineString salvarRota(Point saida, Point destino){
        String resposta = getRota(saida, destino);
        return rotaMapperService.extrairLineString(resposta);
    }

    public List<Map<String, Double>> retornarParaFront(Long id){
        Optional<Rota> rotaOpt = rotaRepository.findById(id);
        Rota rota = rotaOpt.get();
        return rotaMapperService.toReactNative(rota.getTrajeto());
    }

}

