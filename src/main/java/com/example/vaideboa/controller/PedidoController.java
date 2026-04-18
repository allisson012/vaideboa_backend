package com.example.vaideboa.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.vaideboa.Dtos.AgendarCaronaDto;
import com.example.vaideboa.Dtos.ApiResponse;
import com.example.vaideboa.service.PedidoService;

@RestController
@RequestMapping("/pedido")
public class PedidoController {
    
    private final PedidoService pedidoService;
    
    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @PostMapping("/agendar")
    public ResponseEntity<?> agendarCarona(@RequestBody AgendarCaronaDto agendarCaronaDto, Authentication auth){
        String username = auth.getName();
        ApiResponse response = pedidoService.agendarCarona(agendarCaronaDto, username);
        if(!response.isRetorno()){
            return ResponseEntity.badRequest().body(response.getMensagem());
        }
        return ResponseEntity.ok(response.getMensagem());
    }

    @PostMapping("/aceitar/{id}")
    public ResponseEntity<?> aceitarPedidoCarona(@PathVariable Long id, Authentication auth){
        String username = auth.getName();
        ApiResponse resposta = pedidoService.aceitarPedidoCarona(id, username);
        if(!resposta.isRetorno()){
            return ResponseEntity.badRequest().body(resposta.getMensagem());
        }
        return ResponseEntity.ok(resposta.getMensagem());
    }

    @GetMapping("/buscar")
    public ResponseEntity<?> buscarPedidoCarona(Authentication auth){
        String username = auth.getName();
        ApiResponse resposta = pedidoService.buscarPedidos(username);
        if(!resposta.isRetorno()){
            return ResponseEntity.badRequest().body(resposta.getMensagem());
        }
        return ResponseEntity.ok(resposta.getDados());
    }
}
