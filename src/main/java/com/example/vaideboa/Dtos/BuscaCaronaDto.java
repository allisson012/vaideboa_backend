package com.example.vaideboa.Dtos;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class BuscaCaronaDto {
     private final double saidaLat;
     private final double saidaLon;
     private final double destinoLat;
     private final double destinoLon;
     private final LocalDate data;
     private final LocalTime hora;
     
     public BuscaCaronaDto(double saidaLat, double saidaLon, double destinoLat, double destinoLon, LocalDate data,
            LocalTime hora) {
        this.saidaLat = saidaLat;
        this.saidaLon = saidaLon;
        this.destinoLat = destinoLat;
        this.destinoLon = destinoLon;
        this.data = data;
        this.hora = hora;
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
     public LocalDate getData() {
         return data;
     }
     public LocalTime getHora() {
         return hora;
     }

      
    // se o destino for = igual ao destino da carona retornar a carona e ainda tiver espaço no carro
}
