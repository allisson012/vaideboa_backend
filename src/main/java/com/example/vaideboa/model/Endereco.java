package com.example.vaideboa.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Endereco {
    // ideia do endereço é ser para salvar a casa do passageiro
    private String estado;
    private String cidade;
    private String bairro;
    private String rua;
    private String numero;
    // Separar em Estado cidade
}
