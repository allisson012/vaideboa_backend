package com.example.vaideboa.Dtos;

import java.time.LocalDate;
import java.time.LocalTime;

public class CaronaDto {
    private final int qntAssentos;
    private final LocalDate data;
    private final LocalTime hora;
    private final double saidaLat;
    private final double saidaLng;
    private final double destinoLat;
    private final double destinoLng;
    public CaronaDto(int qntAssentos, LocalDate data, LocalTime hora, double saidaLat, double saidaLng,
            double destinoLat, double destinoLng) {
        this.qntAssentos = qntAssentos;
        this.data = data;
        this.hora = hora;
        this.saidaLat = saidaLat;
        this.saidaLng = saidaLng;
        this.destinoLat = destinoLat;
        this.destinoLng = destinoLng;
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
    
    
    
}
