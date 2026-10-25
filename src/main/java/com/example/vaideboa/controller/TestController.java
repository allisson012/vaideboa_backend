package com.example.vaideboa.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.vaideboa.Dtos.RecomendacaoPrecoDto;
import com.example.vaideboa.service.GeoService;
import com.example.vaideboa.service.RecomendacaoPrecoService;
import com.example.vaideboa.service.WebSocketNotificationService;

@RestController
public class TestController {
    private final GeoService geoService;
    private final WebSocketNotificationService webSocketNotificationService;
    private final RecomendacaoPrecoService recomendacaoPrecoService;

    public TestController(GeoService geoService, WebSocketNotificationService webSocketNotificationService,
            RecomendacaoPrecoService recomendacaoPrecoService) {
        this.geoService = geoService;
        this.webSocketNotificationService = webSocketNotificationService;
        this.recomendacaoPrecoService = recomendacaoPrecoService;
    }

    @GetMapping("/teste")
    public String teste() {
        return "ok";
    }

    @GetMapping("/testeGeoCode")
    public String testeGeoCode(){
        return geoService.reverseGeocode(-22.5866443, -44.9625176);
    }

    @GetMapping("/testeWebSocket")
    public String testeWebSocket(){
        return geoService.reverseGeocode(-22.5866443, -44.9625176);
    }
    
    @GetMapping("/websocket")
    public String testar() {

        webSocketNotificationService.enviar(
                "allissonthomas600@gmail.com",
                "Olá! Essa é uma notificação de teste."
        );

        return "Notificação enviada!";
    }

    @GetMapping("/recomendacao")
    public void testarRecomendação(){
        //recomendacaoPrecoService.recomendacaoPreco(63.22,4,null,1L);
    }
}
