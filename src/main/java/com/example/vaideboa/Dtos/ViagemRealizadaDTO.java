package com.example.vaideboa.Dtos;

import java.time.LocalDate;
import java.time.LocalTime;

public class ViagemRealizadaDTO {
    private Long id;
    private double origemLat;
    private double origemLng;
    private double destinoLat;
    private double destinoLng;
    private LocalDate data;
    private LocalTime hora;
    private String papel;

    public ViagemRealizadaDTO(Long id, double origemLat, double origemLng, double destinoLat, double destinoLng, 
            LocalDate data, LocalTime hora, String papel) {
        this.id = id;
        this.origemLat = origemLat;
        this.origemLng = origemLng;
        this.destinoLat = destinoLat;
        this.destinoLng = destinoLng;
        this.data = data;
        this.hora = hora;
        this.papel = papel;
    }

    public Long getId() {
        return id;
    }
    public double getOrigemLat() {
        return origemLat;
    }
    public double getOrigemLng() {
        return origemLng;
    }
    public double getDestinoLat() {
        return destinoLat;
    }
    public double getDestinoLng() {
        return destinoLng;
    }
    public LocalDate getData() {
        return data;
    }
    public LocalTime getHora() {
        return hora;
    }
    public String getPapel() {
        return papel;
    }
}