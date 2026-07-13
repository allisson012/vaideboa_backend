package com.example.vaideboa.Dtos;

public class ValidarCodigoDto {
    private final String email;
    private final String codigo;
    public ValidarCodigoDto(String email, String codigo) {
        this.email = email;
        this.codigo = codigo;
    }
    public String getEmail() {
        return email;
    }
    public String getCodigo() {
        return codigo;
    }
    
}
