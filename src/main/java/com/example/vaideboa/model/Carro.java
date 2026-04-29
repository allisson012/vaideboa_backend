package com.example.vaideboa.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "carro")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Carro {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String marca;
    private String modelo;
    private String cor;
    @Column(unique = true, nullable = false)
    private String placa;
    @Min(1900)
    @Max(2100)
    private Integer ano;
    private String fotoVeiculo;
    // talvez salvar a foto do veiculo
    @Min(1)
    @Max(7) 
    private Integer vagas;
    private Boolean arCondicionado;
    private String descricao;
    private Boolean ativo;
    @ManyToOne
    @JoinColumn(name = "dono_id")
    private User dono;
    @JsonIgnore
    @OneToMany(mappedBy = "carro")
    private List<Carona> caronas;
}
