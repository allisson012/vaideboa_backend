package com.example.vaideboa.Dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LocaisUsuarioDto {
    private final LocalRetornoDto casa;
    private final LocalRetornoDto faculdade;
}
