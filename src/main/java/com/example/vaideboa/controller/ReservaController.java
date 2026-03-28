package com.example.vaideboa.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.vaideboa.Dtos.BuscaCaronaDto;
import com.example.vaideboa.Dtos.BuscaRetornaDto;

import com.example.vaideboa.service.ReservaService;

@RestController
@RequestMapping("/reserva")
public class ReservaController {
    private final ReservaService reservaService;
    
    public ReservaController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    // para retornar as caronas que casam com o local escolhido pelo passageiro
    @PostMapping("/buscar")
    public ResponseEntity<?> buscarCaronas(@RequestBody BuscaCaronaDto buscaDto){
        List<BuscaRetornaDto> buscasRetornoDto = reservaService.buscarCaronas(buscaDto);
        if(buscasRetornoDto.isEmpty()){
            return ResponseEntity.badRequest().body("Erro ao buscar caronas");
        }
        return ResponseEntity.ok(buscasRetornoDto);
    }
}
