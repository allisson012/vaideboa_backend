package com.example.vaideboa.Dtos;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CompartilhamentoCaronaDto {
    private String url;
    private Instant expiraEm;
}
