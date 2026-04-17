package com.example.vaideboa.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.vaideboa.service.GeoService;

@RestController
public class TestController {
    private final GeoService geoService;

    public TestController(GeoService geoService) {
        this.geoService = geoService;
    }
    @GetMapping("/teste")
    public String teste() {
        return "ok";
    }

    @GetMapping("/testeGeoCode")
    public String testeGeoCode(){
        return geoService.reverseGeocode(-22.5866443, -44.9625176);
    }
}
