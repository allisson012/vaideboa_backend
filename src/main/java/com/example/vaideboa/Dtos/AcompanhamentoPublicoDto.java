package com.example.vaideboa.Dtos;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AcompanhamentoPublicoDto {
    private String statusViagem;
    private String estadoLocalizacao;
    private Coordenadas localizacao;
    private Instant ultimaAtualizacaoEm;

    @Data
    @AllArgsConstructor
    public static class Coordenadas {
        private double latitude;
        private double longitude;
    }
}
