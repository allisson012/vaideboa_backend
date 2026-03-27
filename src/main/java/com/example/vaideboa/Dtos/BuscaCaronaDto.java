package com.example.vaideboa.Dtos;

import java.time.LocalDateTime;

public class BuscaCaronaDto {
     private final double saidaLat;
     private final double saidaLon;
     private final double destinoLat;
     private final double destinoLon;
     private final LocalDateTime dataEhora;
     private final Long idUser;
     public BuscaCaronaDto(double saidaLat, double saidaLon, double destinoLat, double destinoLon,
            LocalDateTime dataEhora, Long idUser) {
        this.saidaLat = saidaLat;
        this.saidaLon = saidaLon;
        this.destinoLat = destinoLat;
        this.destinoLon = destinoLon;
        this.dataEhora = dataEhora;
        this.idUser = idUser;
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
     public Long getIdUser() {
         return idUser;
     }
      
    // se o destino for = igual ao destino da carona retornar a carona e ainda tiver espaço no carro
}
