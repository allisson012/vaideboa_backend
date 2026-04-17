package com.example.vaideboa.Dtos;

import org.locationtech.jts.geom.Point;

public class PedidoCaronaRetornoDto {
    private String nome;
    private Double avaliacao;
    // foto se tiver
    // quantidades de avaliações 
    private Point origem; // enviar em formato de texto as informações 
    private Point destino;
    private String data;
    private String hora;
    private int vagasDisponiveis;
    private Long idPedidoCarona;
    private String genero;
    // preferencias do usuario
    private Double distancia;
    private Double duracao;
    public PedidoCaronaRetornoDto() {
    }
    public String getNome() {
        return nome;
    }
    public void setNome(String nome) {
        this.nome = nome;
    }
    public Double getAvaliacao() {
        return avaliacao;
    }
    public void setAvaliacao(Double avaliacao) {
        this.avaliacao = avaliacao;
    }
    public Point getOrigem() {
        return origem;
    }
    public void setOrigem(Point origem) {
        this.origem = origem;
    }
    public Point getDestino() {
        return destino;
    }
    public void setDestino(Point destino) {
        this.destino = destino;
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
    public int getVagasDisponiveis() {
        return vagasDisponiveis;
    }
    public void setVagasDisponiveis(int vagasDisponiveis) {
        this.vagasDisponiveis = vagasDisponiveis;
    }
    public Long getIdPedidoCarona() {
        return idPedidoCarona;
    }
    public void setIdPedidoCarona(Long idPedidoCarona) {
        this.idPedidoCarona = idPedidoCarona;
    }
    public String getGenero() {
        return genero;
    }
    public void setGenero(String genero) {
        this.genero = genero;
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
