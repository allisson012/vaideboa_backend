package com.example.vaideboa.Dtos;

import java.util.List;

import org.springframework.data.domain.Page;

public record MensagensPaginaDto(
        List<MensagemRetornoDto> mensagens,
        int pagina,
        int tamanho,
        long totalMensagens,
        int totalPaginas,
        boolean ultimaPagina) {

    public static MensagensPaginaDto from(Page<MensagemRetornoDto> pagina) {
        return new MensagensPaginaDto(
                pagina.getContent(),
                pagina.getNumber(),
                pagina.getSize(),
                pagina.getTotalElements(),
                pagina.getTotalPages(),
                pagina.isLast());
    }
}
