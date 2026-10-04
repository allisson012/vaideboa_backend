package com.example.vaideboa.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.vaideboa.Dtos.ApiResponse;
import com.example.vaideboa.Dtos.MensagemDto;
import com.example.vaideboa.Dtos.MensagensPaginaDto;
import com.example.vaideboa.service.ChatService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/chat")
public class ChatController {
    private final ChatService chatService;
    
    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/iniciar/{idReserva}")
    public void iniciarChat(@RequestParam Long idReserva, Authentication auth){
        String username = auth.getName();
        
    }

    @PostMapping("/enviarMensagem")
    public ResponseEntity<?> enviarMensagem(@Valid @RequestBody MensagemDto mensagemDto, Authentication auth){
        String username = auth.getName();
        ApiResponse response = chatService.enviarMensagem(username, mensagemDto);
        if(!response.isRetorno()){
            return ResponseEntity.badRequest().body(response.getMensagem());
        }
        return ResponseEntity.ok(response.getDados());
    }

    @GetMapping("/{idReserva}/mensagens")
    public ResponseEntity<MensagensPaginaDto> buscarMensagens(@PathVariable Long idReserva, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "30") int size, Authentication auth) {
        return ResponseEntity.ok(chatService.buscarMensagens(auth.getName(), idReserva, page, size));
    }
}
