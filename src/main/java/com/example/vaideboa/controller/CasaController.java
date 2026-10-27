package com.example.vaideboa.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.vaideboa.Dtos.ApiResponse;
import com.example.vaideboa.Dtos.CasaDto;
import com.example.vaideboa.service.CasaService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/casa")
public class CasaController {
    private final CasaService casaService;

    public CasaController(CasaService casaService) {
        this.casaService = casaService;
    }

    @PostMapping("/cadastrar")
    public ResponseEntity<?> cadastrarCasa(@Valid @RequestBody CasaDto dto, Authentication auth) {
        String username = auth.getName();
        ApiResponse resposta = casaService.cadastrarCasa(username, dto);
        if (!resposta.isRetorno()) {
            return ResponseEntity.badRequest().body(resposta.getMensagem());
        }
        return ResponseEntity.ok(resposta.getMensagem());
    }
}
