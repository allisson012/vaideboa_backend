package com.example.vaideboa.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.vaideboa.Dtos.ApiResponse;
import com.example.vaideboa.Dtos.FaculdadeDto;
import com.example.vaideboa.service.FaculdadeService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/faculdade")
public class FaculdadeController {
    private final FaculdadeService faculdadeService;

    public FaculdadeController(FaculdadeService faculdadeService) {
        this.faculdadeService = faculdadeService;
    }

    @PostMapping("/cadastrar")
    public ResponseEntity<?> cadastrarFaculdade(@Valid @RequestBody FaculdadeDto dto, Authentication auth) {
        String username = auth.getName();
        ApiResponse resposta = faculdadeService.cadastrarFaculdade(username, dto);
        if (!resposta.isRetorno()) {
            return ResponseEntity.badRequest().body(resposta.getMensagem());
        }
        return ResponseEntity.ok(resposta.getMensagem());
    }
}
