package com.example.vaideboa.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.vaideboa.Dtos.ApiResponse;
import com.example.vaideboa.Dtos.RecuperarSenhaDto;
import com.example.vaideboa.Dtos.ValidarCodigoDto;
import com.example.vaideboa.service.RecuperacaoSenhaService;

@RestController
@RequestMapping("/recuperarSenha")
public class RecuperarSenhaController {
   private final RecuperacaoSenhaService recuperacaoSenhaService;

   public RecuperarSenhaController(RecuperacaoSenhaService recuperacaoSenhaService) {
    this.recuperacaoSenhaService = recuperacaoSenhaService;
   }
   // tenho que abrir para chamar esse metodo sem passar o auth
   @GetMapping("/enviarEmail")
   public ResponseEntity<?> enviarEmail(@RequestParam String email){
    ApiResponse response = recuperacaoSenhaService.enviarEmailRecuperacao(email);  
    return ResponseEntity.ok(response.getMensagem());
   } 
   @PostMapping("/validarCodigo")
   public ResponseEntity<?> validarCodigo(@RequestBody ValidarCodigoDto dto){
     ApiResponse response = recuperacaoSenhaService.validarCodigo(dto.getCodigo(), dto.getEmail());
     if(!response.isRetorno()){
      return ResponseEntity.badRequest().body(response.getMensagem());
     }
     return ResponseEntity.ok(response);
   }
   @PostMapping("/alterarSenha")
   public ResponseEntity<?> alterarSenha(@RequestBody RecuperarSenhaDto dto){
    ApiResponse response = recuperacaoSenhaService.alterarSenha(dto);
    if(!response.isRetorno()){
      return ResponseEntity.badRequest().body(response.getMensagem());
    }
    return ResponseEntity.ok(response.getMensagem());
   }
}
