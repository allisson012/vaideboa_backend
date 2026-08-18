package com.example.vaideboa.Dtos;

public class LocalizacaoDto {
    private Long idCarona;
    private double latitude;
    private double longitude;
    public LocalizacaoDto(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }
    public LocalizacaoDto() {
    }
    public Long getIdCarona() {
        return idCarona;
    }
    public void setIdCarona(Long idCarona) {
        this.idCarona = idCarona;
    }
    public double getLatitude() {
        return latitude;
    }
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
    public double getLongitude() {
        return longitude;
    }
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
    
}
