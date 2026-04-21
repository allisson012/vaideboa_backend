package com.example.vaideboa.Dtos;

import java.time.LocalDate;
import java.time.LocalTime;

public class BuscaRetornaDto {
    private  LocalDate data;
    private  LocalTime hora;
    private  Long idMotorista;
    private  String nomeMotorista;
    private  Long idRota;
    private  double saidaLat;
    private  double saidaLon;
    private  double destinoLat;
    private  double destinoLon;
    private  Double distancia;
    private  Double duracao;
    
    public Long getIdMotorista() {
        return idMotorista;
    }
    public void setIdMotorista(Long idMotorista) {
        this.idMotorista = idMotorista;
    }
    public String getNomeMotorista() {
        return nomeMotorista;
    }
    public void setNomeMotorista(String nomeMotorista) {
        this.nomeMotorista = nomeMotorista;
    }
    public Long getIdRota() {
        return idRota;
    }
    public void setIdRota(Long idRota) {
        this.idRota = idRota;
    }
    public double getSaidaLat() {
        return saidaLat;
    }
    public void setSaidaLat(double saidaLat) {
        this.saidaLat = saidaLat;
    }
    public double getSaidaLon() {
        return saidaLon;
    }
    public void setSaidaLon(double saidaLon) {
        this.saidaLon = saidaLon;
    }
    public double getDestinoLat() {
        return destinoLat;
    }
    public void setDestinoLat(double destinoLat) {
        this.destinoLat = destinoLat;
    }
    public double getDestinoLon() {
        return destinoLon;
    }
    public void setDestinoLon(double destinoLon) {
        this.destinoLon = destinoLon;
    }
    public LocalDate getData() {
        return data;
    }
    public void setData(LocalDate data) {
        this.data = data;
    }
    public LocalTime getHora() {
        return hora;
    }
    public void setHora(LocalTime hora) {
        this.hora = hora;
    }
    public Double getDistancia() {
        return distancia;
    }
    public void setDistancia(Double distancia) {
        this.distancia = distancia;
    }
    public Double getDuracao() {
        return duracao;
    }
    public void setDuracao(Double duracao) {
        this.duracao = duracao;
    }

    
}
