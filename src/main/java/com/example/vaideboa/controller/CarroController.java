package com.example.vaideboa.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.vaideboa.Dtos.ApiResponse;
import com.example.vaideboa.Dtos.CarroDto;
import com.example.vaideboa.service.CarroService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/carro")
public class CarroController {
    private final CarroService carroService;
    
    public CarroController(CarroService carroService) {
        this.carroService = carroService;
    }

    @PostMapping("/cadastrar")
    public ResponseEntity<?> cadastrarCarro(@Valid @RequestBody CarroDto carroDto, Authentication auth){
        String username = auth.getName();
        ApiResponse resposta = carroService.cadastrarCarro(username, carroDto);
        if(!resposta.isRetorno()){
            return ResponseEntity.badRequest().body(resposta.getMensagem());
        }
        return ResponseEntity.ok(resposta.getMensagem());
    }
}
