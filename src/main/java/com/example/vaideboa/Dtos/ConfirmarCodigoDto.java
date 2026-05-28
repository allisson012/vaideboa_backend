package com.example.vaideboa.Dtos;

public class ConfirmarCodigoDto {
    private final Long idCarona;
    private final String codigo;
    public ConfirmarCodigoDto(Long idCarona, String codigo) {
        this.idCarona = idCarona;
        this.codigo = codigo;
    }
    public Long getIdCarona() {
        return idCarona;
    }
    public String getCodigo() {
        return codigo;
    }
    
}
