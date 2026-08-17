package com.example.vaideboa.Dtos;

public class PontoParadaRetornoDto {
    private double latPonto;
    private double lonPonto;
    private int indexOrder;
    private String textoPonto;
    
    public PontoParadaRetornoDto() {
    }
    
    public double getLatPonto() {
        return latPonto;
    }
    public void setLatPonto(double latPonto) {
        this.latPonto = latPonto;
    }
    public double getLonPonto() {
        return lonPonto;
    }
    public void setLonPonto(double lonPonto) {
        this.lonPonto = lonPonto;
    }
    public int getIndexOrder() {
        return indexOrder;
    }
    public void setIndexOrder(int indexOrder) {
        this.indexOrder = indexOrder;
    }
    public String getTextoPonto() {
        return textoPonto;
    }
    public void setTextoPonto(String textoPonto) {
        this.textoPonto = textoPonto;
    }
    
}
