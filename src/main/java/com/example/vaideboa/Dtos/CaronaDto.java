package com.example.vaideboa.Dtos;

import java.time.LocalDateTime;

public class CaronaDto {
    private final int qntAssentos;
    private final LocalDateTime dataHora;
    private final Long userId;
    private final double saidaLat;
    private final double saidaLng;
    private final double destinoLat;
    private final double destinoLng;
    
    public CaronaDto(int qntAssentos, LocalDateTime dataHora, Long userId, double saidaLat, double saidaLng,
            double destinoLat, double destinoLng) {
        this.qntAssentos = qntAssentos;
        this.dataHora = dataHora;
        this.userId = userId;
        this.saidaLat = saidaLat;
        this.saidaLng = saidaLng;
        this.destinoLat = destinoLat;
        this.destinoLng = destinoLng;
    }
    
    public int getQntAssentos() {
        return qntAssentos;
    }
    public LocalDateTime getDataHora() {
        return dataHora;
    }
    public Long getUserId() {
        return userId;
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
