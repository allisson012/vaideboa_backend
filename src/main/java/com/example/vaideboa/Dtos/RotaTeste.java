package com.example.vaideboa.Dtos;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RotaTeste {
    private double latSaida;
    private double lonSaida;
    private double latDestino;
    private double lonDestino;
    private List<ParadaDto> paradas;
}
