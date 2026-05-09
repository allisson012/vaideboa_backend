package com.example.vaideboa.Dtos;

import com.example.vaideboa.model.enums.NivelPreferencia;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PreferenciasDto {
    private final NivelPreferencia conversa;
    private final NivelPreferencia musica;
    private final NivelPreferencia cigarro;
    private final NivelPreferencia animais;
    
}
