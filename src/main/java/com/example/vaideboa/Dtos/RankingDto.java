package com.example.vaideboa.Dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RankingDto {
    private Double notaMotorista;
    private Double notaPassageiro;
    private Integer numViagensMotorista;
    private Integer numViagensPassageiro;
}
