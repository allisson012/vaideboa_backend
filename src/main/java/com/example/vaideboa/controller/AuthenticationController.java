package com.example.vaideboa.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.vaideboa.Dtos.LoginResponseDto;
import com.example.vaideboa.service.AuthenticationService;

@RestController
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/authenticate")
    public String authenticate(@RequestBody LoginResponseDto loginResponseDto){
        return authenticationService.authenticate(loginResponseDto);
    }

    @GetMapping("/login/sucesso")
    public Object loginSucesso(Authentication auth){
        return auth.getPrincipal();
    }

    @GetMapping("/validarToken")
    public ResponseEntity<Boolean> validarToken(@RequestParam String token){ 
        //String token = authHeader.replace("Bearer ", "");
        boolean valido = authenticationService.tokenValido(token);
        return ResponseEntity.ok(valido);
    }
}