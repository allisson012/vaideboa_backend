package com.example.vaideboa.Dtos;

public class UserRetornoDto {
    private final String nome;
    private final String email;
    private final String cpf;
    private final String telefone;
    private final String dataNascimento;
    private final String genero;
    private final PreferenciasDto preferenciasDto;
    
    public UserRetornoDto(String nome, String email, String cpf, String telefone, String dataNascimento, String genero,
            PreferenciasDto preferenciasDto) {
        this.nome = nome;
        this.email = email;
        this.cpf = cpf;
        this.telefone = telefone;
        this.dataNascimento = dataNascimento;
        this.genero = genero;
        this.preferenciasDto = preferenciasDto;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public String getCpf() {
        return cpf;
    }

    public String getTelefone() {
        return telefone;
    }

    public String getDataNascimento() {
        return dataNascimento;
    }

    public String getGenero() {
        return genero;
    }

    public PreferenciasDto getPreferenciasDto() {
        return preferenciasDto;
    }
}
