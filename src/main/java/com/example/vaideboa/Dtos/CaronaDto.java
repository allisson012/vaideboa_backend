package com.example.vaideboa.Dtos;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class CaronaDto {
    private final int qntAssentos;
    private final LocalDate data;
    private final LocalTime hora;
    private final double saidaLat;
    private final double saidaLng;
    private final double destinoLat;
    private final double destinoLng;
    private final List<ParadaDto> paradas;

    public CaronaDto(int qntAssentos, LocalDate data, LocalTime hora, double saidaLat, double saidaLng,
            double destinoLat, double destinoLng, List<ParadaDto> paradas) {
        this.qntAssentos = qntAssentos;
        this.data = data;
        this.hora = hora;
        this.saidaLat = saidaLat;
        this.saidaLng = saidaLng;
        this.destinoLat = destinoLat;
        this.destinoLng = destinoLng;
        this.paradas = paradas;
    }
    
    public int getQntAssentos() {
        return qntAssentos;
    }
    public LocalDate getData() {
        return data;
    }
    public LocalTime getHora() {
        return hora;
    }
    public double getSaidaLat() {
        return saidaLat;
    }
    public double getSaidaLng() {
        return saidaLng;
    }
    public double getDestinoLat() {
        return destinoLat;
    }
    public double getDestinoLng() {
        return destinoLng;
    }

    public List<ParadaDto> getParadas() {
        return paradas;
    }    
}
