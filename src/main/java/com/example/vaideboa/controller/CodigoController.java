package com.example.vaideboa.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.vaideboa.Dtos.ApiResponse;
import com.example.vaideboa.Dtos.ConfirmarCodigoDto;
import com.example.vaideboa.service.CodigoService;

@RestController
@RequestMapping("/codigo")
public class CodigoController {
  private final CodigoService codigoService;
  public CodigoController(CodigoService codigoService) {
    this.codigoService = codigoService;
  }
  @PostMapping("/confirmar")
  public ResponseEntity<?> confirmarCodigo(@RequestBody ConfirmarCodigoDto dto, Authentication auth){
   String username = auth.getName();
   ApiResponse resposta = codigoService.confirmarCodigo(dto, username);
   if(!resposta.isRetorno()){
    return ResponseEntity.badRequest().body(resposta.getMensagem());
   }
   return ResponseEntity.ok(resposta.getMensagem());
  }
}
