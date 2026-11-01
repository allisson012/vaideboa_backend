package com.example.vaideboa.Dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LocalRetornoDto {
    private final Long id;
    private final String descricao;
    private final double latitude;
    private final double longitude;
}
