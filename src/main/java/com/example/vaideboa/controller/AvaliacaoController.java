package com.example.vaideboa.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.vaideboa.Dtos.ApiResponse;
import com.example.vaideboa.Dtos.AvaliacaoDto;
import com.example.vaideboa.service.AvaliacaoService;

@RestController
@RequestMapping("/avaliacao")
public class AvaliacaoController {
    private final AvaliacaoService avaliacaoService;
    
    public AvaliacaoController(AvaliacaoService avaliacaoService) {
        this.avaliacaoService = avaliacaoService;
    }

    @PostMapping("/cadastrar")
    public ResponseEntity<?> cadastrarAvaliacao(@RequestBody AvaliacaoDto avaliacaoDto, Authentication auth){
        String username = auth.getName();
        ApiResponse retorno = avaliacaoService.cadastrarAvaliacao(avaliacaoDto, username);
        if(!retorno.isRetorno()){
            return  ResponseEntity.badRequest().body(retorno.getMensagem());
        }
        return ResponseEntity.ok(retorno.getMensagem());
    } 
}
