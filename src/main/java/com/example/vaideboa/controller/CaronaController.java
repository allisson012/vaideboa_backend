package com.example.vaideboa.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.vaideboa.Dtos.ApiResponse;
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
    public ResponseEntity<?> cadastrarCarona(@RequestBody CaronaDto caronaDto, Authentication auth){
        String username = auth.getName();
        boolean retorno = caronaService.cadastrarCarona(caronaDto, username);
        if(retorno == false)
        {
            // melhorar o retorno pois esta muito subjetivo
            return ResponseEntity.badRequest().body("Erro ao criar carona");
        }
        return ResponseEntity.ok("Carona criada com sucesso");
    }
     
    @GetMapping("/minhas")
    public ResponseEntity<?> minhasViagens(Authentication auth, @RequestParam (required = false) String tipo) {
        String username = auth.getName();
        ApiResponse response = caronaService.minhasViagens(username, tipo);
        if (!response.isRetorno()) {
        return ResponseEntity.badRequest().body(response.getMensagem());
        }
        return ResponseEntity.ok(response.getDados());
    }

    @GetMapping("/finalizar/{idCarona}")
    public ResponseEntity<?> finalizarCarona(@PathVariable Long idCarona, Authentication auth){
     String username = auth.getName();
     ApiResponse retorno = caronaService.finalizarCorrida(idCarona, username);
     if(!retorno.isRetorno()){
        return ResponseEntity.badRequest().body(retorno.getMensagem());
     }
     return ResponseEntity.ok(retorno.getMensagem());
    }

}
