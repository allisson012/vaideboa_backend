package com.example.vaideboa.Dtos;

public record PerfilPublicoDto(
    Long id,
    String nome,
    String foto,
    String genero,
    PreferenciasDto preferenciasDto,
    RankingDto rankingDto
) {
}
