package com.example.vaideboa.controller;

import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import com.example.vaideboa.Dtos.ApiResponse;
import com.example.vaideboa.service.CompartilhamentoExternoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/carona")
@RequiredArgsConstructor
@Slf4j
public class CompartilhamentoCaronaController {
    private final CompartilhamentoExternoService compartilhamentoExternoService;

    @PostMapping("/{idCarona}/compartilhamento")
    public ResponseEntity<?> obterLink(@PathVariable Long idCarona, Authentication auth) {
        try {
            return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(compartilhamentoExternoService.obterOuCriarLink(idCarona, auth.getName()));
        } catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode()).cacheControl(CacheControl.noStore()).body(new ApiResponse(false, "Compartilhamento indisponível para esta solicitação."));
        } catch (RuntimeException ex) {
            log.error("Falha ao obter compartilhamento público: {}", ex.getClass().getSimpleName());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).cacheControl(CacheControl.noStore()).body(new ApiResponse(false, "Compartilhamento temporariamente indisponível."));
        }
    }
}
