package com.example.vaideboa.Dtos;

public class ParadaDto {
    private final int indexOrder;
    private final double latitude;
    private final double longitude;
    public ParadaDto(int indexOrder, double latitude, double longitude) {
        this.indexOrder = indexOrder;
        this.latitude = latitude;
        this.longitude = longitude;
    }
    public int getIndexOrder() {
        return indexOrder;
    }
    public double getLatitude() {
        return latitude;
    }
    public double getLongitude() {
        return longitude;
    }
    
}
