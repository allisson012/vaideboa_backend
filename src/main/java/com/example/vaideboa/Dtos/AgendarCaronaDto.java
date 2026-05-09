package com.example.vaideboa.Dtos;


public class AgendarCaronaDto {
    private final Long idCarona;
    private final double saidaLat;
    private final double saidaLng;
    private final double destinoLat;
    private final double destinoLng;
    
    public AgendarCaronaDto(Long idCarona, double saidaLat, double saidaLng, double destinoLat, double destinoLng) {
        this.idCarona = idCarona;
        this.saidaLat = saidaLat;
        this.saidaLng = saidaLng;
        this.destinoLat = destinoLat;
        this.destinoLng = destinoLng;
    }
    public Long getIdCarona() {
        return idCarona;
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
