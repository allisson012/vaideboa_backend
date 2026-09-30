package com.example.vaideboa.Dtos;

public class RecomendacaoPrecoDto {

    private Double distancia;
    private Double consumo;
    private Double precoGasolina;
    private Double litrosNecessarios;
    private Double custoCombustivel;
    private Integer quantidadePessoas;
    private Double precoPorPessoa;

    public RecomendacaoPrecoDto() {
    }

    public RecomendacaoPrecoDto(
            Double distancia,
            Double consumo,
            Double precoGasolina,
            Double litrosNecessarios,
            Double custoCombustivel,
            Integer quantidadePessoas,
            Double precoPorPessoa
    ) {
        this.distancia = distancia;
        this.consumo = consumo;
        this.precoGasolina = precoGasolina;
        this.litrosNecessarios = litrosNecessarios;
        this.custoCombustivel = custoCombustivel;
        this.quantidadePessoas = quantidadePessoas;
        this.precoPorPessoa = precoPorPessoa;
    }

    public Double getDistancia() {
        return distancia;
    }

    public void setDistancia(Double distancia) {
        this.distancia = distancia;
    }

    public Double getConsumo() {
        return consumo;
    }

    public void setConsumo(Double consumo) {
        this.consumo = consumo;
    }

    public Double getPrecoGasolina() {
        return precoGasolina;
    }

    public void setPrecoGasolina(Double precoGasolina) {
        this.precoGasolina = precoGasolina;
    }

    public Double getLitrosNecessarios() {
        return litrosNecessarios;
    }

    public void setLitrosNecessarios(Double litrosNecessarios) {
        this.litrosNecessarios = litrosNecessarios;
    }

    public Double getCustoCombustivel() {
        return custoCombustivel;
    }

    public void setCustoCombustivel(Double custoCombustivel) {
        this.custoCombustivel = custoCombustivel;
    }

    public Integer getQuantidadePessoas() {
        return quantidadePessoas;
    }

    public void setQuantidadePessoas(Integer quantidadePessoas) {
        this.quantidadePessoas = quantidadePessoas;
    }

    public Double getPrecoPorPessoa() {
        return precoPorPessoa;
    }

    public void setPrecoPorPessoa(Double precoPorPessoa) {
        this.precoPorPessoa = precoPorPessoa;
    }
}