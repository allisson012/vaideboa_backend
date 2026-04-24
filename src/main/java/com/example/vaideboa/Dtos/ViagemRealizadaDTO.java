package com.example.vaideboa.Dtos;

import java.time.LocalDate;
import java.time.LocalTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ViagemRealizadaDTO {
    private Long id;
    private double origemLat;
    private double origemLng;
    private double destinoLat;
    private double destinoLng;
    private LocalDate data;
    private LocalTime hora;
    private String papel;
    private boolean realizada;
    private String origemTexto;
    private String destinoTexto;


}