package com.example.vaideboa.Dtos;

public class CaronaRetornoDto {
    private Long idCarona;
    private int qntAssentos;
    private int vagasDisponiveis;
    private String data;
    private String hora;
    private boolean realizado;
    private double latSaida; 
    private double lonSaida;
    private String saidaTexto;
    private double latDestino;
    private double lonDestino;
    private String destinoTexto;
    private Double distancia;
    private Double duracao;
    private Long idRota;
    // trajeto
    // id do user se for para o motorista 
    private String nome;
    private String genero;
    // carro
    // foto
    public Long getIdCarona() {
        return idCarona;
    }
    public void setIdCarona(Long idCarona) {
        this.idCarona = idCarona;
    }
    public int getQntAssentos() {
        return qntAssentos;
    }
    public void setQntAssentos(int qntAssentos) {
        this.qntAssentos = qntAssentos;
    }
    public int getVagasDisponiveis() {
        return vagasDisponiveis;
    }
    public void setVagasDisponiveis(int vagasDisponiveis) {
        this.vagasDisponiveis = vagasDisponiveis;
    }
    public String getData() {
        return data;
    }
    public void setData(String data) {
        this.data = data;
    }
    public String getHora() {
        return hora;
    }
    public void setHora(String hora) {
        this.hora = hora;
    }
    public boolean isRealizado() {
        return realizado;
    }
    public void setRealizado(boolean realizado) {
        this.realizado = realizado;
    }
    public double getLatSaida() {
        return latSaida;
    }
    public void setLatSaida(double latSaida) {
        this.latSaida = latSaida;
    }
    public double getLonSaida() {
        return lonSaida;
    }
    public void setLonSaida(double lonSaida) {
        this.lonSaida = lonSaida;
    }
    public String getSaidaTexto() {
        return saidaTexto;
    }
    public void setSaidaTexto(String saidaTexto) {
        this.saidaTexto = saidaTexto;
    }
    public double getLatDestino() {
        return latDestino;
    }
    public void setLatDestino(double latDestino) {
        this.latDestino = latDestino;
    }
    public double getLonDestino() {
        return lonDestino;
    }
    public void setLonDestino(double lonDestino) {
        this.lonDestino = lonDestino;
    }
    public String getDestinoTexto() {
        return destinoTexto;
    }
    public void setDestinoTexto(String destinoTexto) {
        this.destinoTexto = destinoTexto;
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
    public String getNome() {
        return nome;
    }
    public void setNome(String nome) {
        this.nome = nome;
    }
    public String getGenero() {
        return genero;
    }
    public void setGenero(String genero) {
        this.genero = genero;
    }
    public Long getIdRota() {
        return idRota;
    }
    public void setIdRota(Long idRota) {
        this.idRota = idRota;
    }
    
}
