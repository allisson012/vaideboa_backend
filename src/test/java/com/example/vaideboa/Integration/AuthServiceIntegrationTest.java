package com.example.vaideboa.Integration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.transaction.annotation.Transactional;

import com.example.vaideboa.Dtos.LoginResponseDto;
import com.example.vaideboa.service.AuthenticationService;

@SpringBootTest
@Transactional
class AuthServiceIntegrationTest {

    @Autowired
    private AuthenticationService authService;

    @Test
    void deveAutenticarUsuarioReal() {

        LoginResponseDto dto = new LoginResponseDto("admin@gmail.com","123456");


        String token = authService.authenticate(dto);

        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void naoDeveAutenticarUsuarioComSenhaInvalida() {

        LoginResponseDto dto = new LoginResponseDto("admin@gmail.com","senhaErrada");

        assertThrows(
                BadCredentialsException.class,
                () -> authService.authenticate(dto)
        );
    }
}
