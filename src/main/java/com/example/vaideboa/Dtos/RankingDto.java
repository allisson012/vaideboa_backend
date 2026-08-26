package com.example.vaideboa.Dtos;

import com.example.vaideboa.model.enums.Ranking;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RankingDto {
    private Double notaMotorista;
    private Double notaPassageiro;
    private Integer numAvaliacoesMotorista;
    private Integer numAvaliacoesPassageiro;
    private Integer numViagensMotorista;
    private Integer numViagensPassageiro;
    private Ranking rankingMotorista;
}
