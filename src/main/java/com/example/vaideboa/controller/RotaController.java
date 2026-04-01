package com.example.vaideboa.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.example.vaideboa.Dtos.RotaTeste;
import com.example.vaideboa.service.RotaService;

import org.apache.catalina.connector.Response;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;


import java.util.*;

@RestController
@RequestMapping("/rota")
public class RotaController {

    @Value("${api.key}")
    private String apiKey;
    private GeometryFactory geometryFactory = new GeometryFactory();
    private final RotaService rotaService;

    public RotaController(RotaService rotaService) {
        this.rotaService = rotaService;
    }

    @GetMapping
    public String getRota() {

    RestTemplate restTemplate = new RestTemplate();
    String url = "https://api.openrouteservice.org/v2/directions/driving-car/geojson";

    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + apiKey);
    headers.setContentType(MediaType.APPLICATION_JSON);

    Map<String, Object> body = new HashMap<>();

    body.put("coordinates", Arrays.asList(
            Arrays.asList(-44.773763, -22.537372),
            Arrays.asList(-44.9081407, -22.5089988),
            Arrays.asList(-44.9625176, -22.5866443)
    ));
    body.put("format", "geojson");
    
    HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
    
    ResponseEntity<String> response =
            restTemplate.postForEntity(url, request, String.class);

    return response.getBody();
    }

    @PostMapping("/buscar")
    public String buscarRota(@RequestBody RotaTeste rotaTeste){
      Point saida = geometryFactory.createPoint(
        new Coordinate(rotaTeste.getLonSaida(), rotaTeste.getLatSaida())
      );
      Point destino = geometryFactory.createPoint(
        new Coordinate(rotaTeste.getLonDestino(), rotaTeste.getLatDestino())
      );
      return rotaService.getRota(saida, destino);
    }

    @PostMapping("/calcular")
    public ResponseEntity<String> calcularDistancia(@RequestBody RotaTeste rotaTeste){
      Point saida = geometryFactory.createPoint(
        new Coordinate(rotaTeste.getLonSaida(), rotaTeste.getLatSaida())
      );
      Point destino = geometryFactory.createPoint(
        new Coordinate(rotaTeste.getLonDestino(), rotaTeste.getLatDestino())
      );

      String kms = rotaService.calcularDistancia(saida, destino);

      return ResponseEntity.ok(kms);
    }

    // criar metodo para buscar a distancia entre dois pontos

}