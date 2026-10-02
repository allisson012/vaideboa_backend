package com.example.vaideboa.Dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class MensagemDto {
    @NotNull(message = "O ID da reserva é obrigatório")
    @Positive(message = "O ID da reserva deve ser positivo")
    private final Long idReserva;
    @NotBlank(message = "A mensagem não pode estar vazia")
    @Size(max = 1000, message = "A mensagem deve ter no máximo 1000 caracteres")
    private final String mensagem;

    public MensagemDto(Long idReserva, String mensagem) {
        this.idReserva = idReserva;
        this.mensagem = mensagem;
    }

    public Long getIdReserva() {
        return idReserva;
    }

    public String getMensagem() {
        return mensagem;
    }
    
    
}
