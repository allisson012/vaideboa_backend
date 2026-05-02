package com.example.vaideboa.Dtos;

import java.time.LocalDate;

import com.example.vaideboa.model.enums.Generos;

public class EditarUserDto {
    private final String nome;
    private final String telefone;
    private final LocalDate dataNascimento;
    private final Generos genero;
    private final String cpf;

    public EditarUserDto(String nome, String telefone, LocalDate dataNascimento, Generos genero, String cpf) {
        this.nome = nome;
        this.telefone = telefone;
        this.dataNascimento = dataNascimento;
        this.genero = genero;
        this.cpf = cpf;
    }
    public String getNome() {
        return nome;
    }
    public String getTelefone() {
        return telefone;
    }
    public LocalDate getDataNascimento() {
        return dataNascimento;
    }
    public Generos getGenero() {
        return genero;
    }
    public String getCpf() {
        return cpf;
    }
}
