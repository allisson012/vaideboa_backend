package com.example.vaideboa.controller;

import java.util.Map;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import com.example.vaideboa.service.CompartilhamentoExternoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/compartilhamento/publico")
@RequiredArgsConstructor
@Slf4j
public class CompartilhamentoPublicoController {
    private final CompartilhamentoExternoService compartilhamentoExternoService;

    @GetMapping("/{token}")
    public ResponseEntity<?> consultar(@PathVariable String token) {
        try {
            return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                    .header("Referrer-Policy", "no-referrer")
                    .body(compartilhamentoExternoService.consultar(token));
        } catch (ResponseStatusException ex) {
            return indisponivel(HttpStatus.NOT_FOUND, "ACOMPANHAMENTO_INDISPONIVEL",
                    "Este acompanhamento não está disponível.");
        } catch (RuntimeException ex) {
            log.error("Falha na consulta pública: {}", ex.getClass().getSimpleName());
            return indisponivel(HttpStatus.SERVICE_UNAVAILABLE, "SERVICO_INDISPONIVEL",
                    "Acompanhamento temporariamente indisponível.");
        }
    }

    private ResponseEntity<?> indisponivel(HttpStatus status, String codigo, String mensagem) {
        return ResponseEntity.status(status).cacheControl(CacheControl.noStore())
                .header("Referrer-Policy", "no-referrer")
                .body(Map.of("codigo", codigo, "mensagem", mensagem));
    }
}
