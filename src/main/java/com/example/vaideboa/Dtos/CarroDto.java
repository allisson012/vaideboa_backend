package com.example.vaideboa.Dtos;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CarroDto {

    @NotBlank(message = "Marca é obrigatória")
    private String marca;

    @NotBlank(message = "Modelo é obrigatório")
    private String modelo;

    @NotBlank(message = "Cor é obrigatória")
    private String cor;

    @NotBlank(message = "Placa é obrigatória")
    @Size(min = 7, max = 7, message = "Placa deve ter 7 caracteres")
    @Pattern(
    regexp = "^[A-Z]{3}[0-9][A-Z0-9][0-9]{2}$",
    message = "Placa inválida"
    )
    private String placa;

    @NotNull(message = "Ano é obrigatório")
    @Min(value = 1900, message = "Ano inválido")
    @Max(value = 2100, message = "Ano inválido")
    private Integer ano;

    private String fotoVeiculo;

    @NotNull(message = "Quantidade de vagas é obrigatória")
    @Min(value = 1, message = "Mínimo de 1 vaga")
    @Max(value = 7, message = "Máximo de 7 vagas")
    private Integer vagas;

    private Boolean arCondicionado;

    @Size(max = 255, message = "Descrição muito longa")
    private String descricao;

}