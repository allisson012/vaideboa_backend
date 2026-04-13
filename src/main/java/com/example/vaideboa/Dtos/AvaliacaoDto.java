package com.example.vaideboa.Dtos;

public class AvaliacaoDto {
  private final Long idReserva;  
  private final double estrela;
  private final String mensagem;
  public AvaliacaoDto(Long idReserva, double estrela, String mensagem) {
    this.idReserva = idReserva;
    this.estrela = estrela;
    this.mensagem = mensagem;
  }
  public Long getIdReserva() {
    return idReserva;
  }
  public double getEstrela() {
    return estrela;
  }
  public String getMensagem() {
    return mensagem;
  }
  
}
