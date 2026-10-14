package com.example.vaideboa.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.vaideboa.Dtos.RecomendacaoPrecoDto;
import com.example.vaideboa.service.RecomendacaoPrecoService;

@RestController 
@RequestMapping("/recomendacaoPreco")
public class RecomendacaoPrecoController {
    private final RecomendacaoPrecoService recomendacaoPrecoService;
    
    public RecomendacaoPrecoController(RecomendacaoPrecoService recomendacaoPrecoService) {
        this.recomendacaoPrecoService = recomendacaoPrecoService;
    }

    @GetMapping("/buscar")
    public ResponseEntity<?> recomendacaoPreco(RecomendacaoPrecoDto recomendacaoDto, Authentication auth){
        recomendacaoPrecoService.recomendacaoPreco(recomendacaoDto.getDistancia(),recomendacaoDto.getQuantidadePessoas(),null,1L);

        return null;
    }
}
