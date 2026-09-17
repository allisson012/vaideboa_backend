package com.example.vaideboa.Dtos;

import com.example.vaideboa.model.Carro;

public record CarroRetornoDto(
    Long id,
    String marca,
    String modelo,
    String cor,
    String placa,
    Integer ano,
    Integer vagas,
    Boolean arCondicionado,
    String descricao,
    String fotoVeiculo
) {
    public static CarroRetornoDto from(Carro carro, boolean ocultarPlaca) {
        String placa = carro.getPlaca();
        if (ocultarPlaca && placa != null && placa.length() == 7) {
            placa = placa.substring(0, 3) + "****";
        }

        return new CarroRetornoDto(
            carro.getId(), carro.getMarca(), carro.getModelo(), carro.getCor(), placa,
            carro.getAno(), carro.getVagas(), carro.getArCondicionado(),
            carro.getDescricao(), carro.getFotoVeiculo()
        );
    }
}
