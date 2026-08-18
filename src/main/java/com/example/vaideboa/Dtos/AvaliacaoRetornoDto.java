package com.example.vaideboa.Dtos;

import com.example.vaideboa.model.enums.TipoAvaliacao;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AvaliacaoRetornoDto {
    private Long id;
    private Long idReserva;
    private Long idCarona;
    private String nomeAvaliado;
    private String data;
    private String hora;
    private String saidaTexto;
    private String destinoTexto;
    private Double nota;
    private String comentario;
    private TipoAvaliacao tipo;
}
