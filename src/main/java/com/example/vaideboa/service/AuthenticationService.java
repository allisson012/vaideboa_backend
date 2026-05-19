package com.example.vaideboa.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.example.vaideboa.Dtos.LoginResponseDto;
import com.example.vaideboa.security.JwtService;

@Service
public class AuthenticationService {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthenticationService(AuthenticationManager authenticationManager,
                                 JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public String authenticate(LoginResponseDto loginResponseDto) {

        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginResponseDto.getUsername(),
                loginResponseDto.getPassword()
            )
        );

        return jwtService.generateToken(authentication);
    }

    public boolean tokenValido(String token) {
    return jwtService.tokenValido(token);
    }

}
