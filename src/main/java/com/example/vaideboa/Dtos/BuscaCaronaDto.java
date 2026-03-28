package com.example.vaideboa.Dtos;

import java.time.LocalDateTime;

public class BuscaCaronaDto {
     private final double saidaLat;
     private final double saidaLon;
     private final double destinoLat;
     private final double destinoLon;
     private final LocalDateTime dataEhora;
     public BuscaCaronaDto(double saidaLat, double saidaLon, double destinoLat, double destinoLon,
            LocalDateTime dataEhora) {
        this.saidaLat = saidaLat;
        this.saidaLon = saidaLon;
        this.destinoLat = destinoLat;
        this.destinoLon = destinoLon;
        this.dataEhora = dataEhora;
     }
     public double getSaidaLat() {
         return saidaLat;
     }
     public double getSaidaLon() {
         return saidaLon;
     }
     public double getDestinoLat() {
         return destinoLat;
     }
     public double getDestinoLon() {
         return destinoLon;
     }
     public LocalDateTime getDataEhora() {
         return dataEhora;
     }

      
    // se o destino for = igual ao destino da carona retornar a carona e ainda tiver espaço no carro
}
