package com.example.vaideboa.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.vaideboa.Dtos.CaronaDto;
import com.example.vaideboa.service.CaronaService;

@RestController
@RequestMapping("/carona")
public class CaronaController {
    private final CaronaService caronaService;
    
    public CaronaController(CaronaService caronaService) {
        this.caronaService = caronaService;
    }

    @PostMapping("/cadastrar")
    public ResponseEntity<?> cadastrarCarona(@RequestBody CaronaDto caronaDto){
        boolean retorno = caronaService.cadastrarCarona(caronaDto);
        if(retorno == false)
        {
            return ResponseEntity.badRequest().body("Erro ao criar carona");
        }
        return ResponseEntity.ok("Carona criada com sucesso");
    }
}
