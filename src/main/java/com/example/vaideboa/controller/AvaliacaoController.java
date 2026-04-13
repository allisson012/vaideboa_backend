package com.example.vaideboa.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.vaideboa.Dtos.AvaliacaoDto;

@RestController
@RequestMapping("/avaliacao")
public class AvaliacaoController {
    
    @PostMapping("/cadastrar")
    public ResponseEntity<?> cadastrarAvaliacao(@RequestBody AvaliacaoDto avaliacaoDto, Authentication auth){
        // 
        String username = auth.getName();
        return null;
    } 
}
