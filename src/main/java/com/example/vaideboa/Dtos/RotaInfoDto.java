package com.example.vaideboa.Dtos;

public class RotaInfoDto {
    private double distanciaKm;
    private double duracaoMin;
    public RotaInfoDto(double distanciaKm, double duracaoMin) {
        this.distanciaKm = distanciaKm;
        this.duracaoMin = duracaoMin;
    }
    public RotaInfoDto() {
    }
    public double getDistanciaKm() {
        return distanciaKm;
    }
    public void setDistanciaKm(double distanciaKm) {
        this.distanciaKm = distanciaKm;
    }
    public double getDuracaoMin() {
        return duracaoMin;
    }
    public void setDuracaoMin(double duracaoMin) {
        this.duracaoMin = duracaoMin;
    }
    
}
