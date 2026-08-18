package com.example.vaideboa.Dtos;

import java.util.List;

import org.locationtech.jts.geom.Point;
import org.springframework.cglib.core.Local;

public class PedidoCaronaRetornoDto {
    private Long idUser;
    private String nome;
    private String foto;
    private Double avaliacao;
    // foto se tiver
    // quantidades de avaliações 
    private double latSaida; // enviar em formato de texto as informações 
    private double lonSaida;
    private String saidaTexto;
    private double latDestino;
    private double lonDestino;
    private String destinoTexto;
    private String data;
    private String dataPedido;
    private String hora;
    private int vagasDisponiveis;
    private Long idCarona;
    private Long idPedidoCarona;
    private String genero;
    // preferencias do usuario
    private Double distancia;
    private Double duracao;
    private String statusPedido;
    private List<PontoParadaRetornoDto> paradas;
    private Long idAvaliacao;
    private Long idReserva;

    public Long getIdUser() {
        return idUser;
    }
    public void setIdUser(Long idUser) {
        this.idUser = idUser;
    }
    public String getStatusPedido() {
        return statusPedido;
    }
    public void setStatusPedido(String statusPedido) {
        this.statusPedido = statusPedido;
    }
    // carro
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
    public String getFoto() {
        return foto;
    }
    public void setFoto(String foto) {
        this.foto = foto;
    }
    public String getDataPedido() {
        return dataPedido;
    }
    public void setDataPedido(String dataPedido) {
        this.dataPedido = dataPedido;
    }
    public Long getIdCarona() {
        return idCarona;
    }
    public void setIdCarona(Long idCarona) {
        this.idCarona = idCarona;
    }
    public List<PontoParadaRetornoDto> getParadas() {
        return paradas;
    }
    public void setParadas(List<PontoParadaRetornoDto> paradas) {
        this.paradas = paradas;
    }
    public Long getIdAvaliacao() {
        return idAvaliacao;
    }
    public void setIdAvaliacao(Long idAvaliacao) {
        this.idAvaliacao = idAvaliacao;
    }
    public Long getIdReserva() {
        return idReserva;
    }
    public void setIdReserva(Long idReserva) {
        this.idReserva = idReserva;
    }
    
}
