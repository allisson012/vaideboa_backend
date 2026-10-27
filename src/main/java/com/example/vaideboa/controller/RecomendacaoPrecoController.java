package com.example.vaideboa.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.vaideboa.Dtos.ApiResponse;
import com.example.vaideboa.service.RecomendacaoPrecoService;

@RestController 
@RequestMapping("/recomendacaoPreco")
public class RecomendacaoPrecoController {
    private final RecomendacaoPrecoService recomendacaoPrecoService;
    
    public RecomendacaoPrecoController(RecomendacaoPrecoService recomendacaoPrecoService) {
        this.recomendacaoPrecoService = recomendacaoPrecoService;
    }

    @GetMapping("/buscar")
    public ResponseEntity<?> recomendacaoPreco(@RequestParam("distancia") Double distancia,@RequestParam("quantidadePessoas") Integer quantidadePessoas, @RequestParam("idCarro") Long idCarro, Authentication auth) {
        ApiResponse resposta = recomendacaoPrecoService.recomendacaoPreco(distancia, quantidadePessoas, idCarro, auth.getName());
        if (!resposta.isRetorno()) {
            return ResponseEntity.badRequest().body(resposta.getMensagem());
        }
        return ResponseEntity.ok(resposta.getDados());
    }
}
