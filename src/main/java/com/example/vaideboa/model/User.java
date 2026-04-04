package com.example.vaideboa.model;

import java.time.LocalDate;
import java.util.List;

import com.example.vaideboa.model.enums.Generos;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nome;
    @Column(unique = true, nullable = false)
    private String username;
    private String password;
    private String cpf;
    private String telefone;
    private LocalDate dataNascimento;
    //private String foto; // caminho ou talvez guardar a foto mesmo
    // foto do documento e selfie verificação manual por parte da equipe 
    private boolean contaNaoExpirada = true;
    private boolean contaNaoBloqueada = true;
    private boolean credenciaisNaoExpiradas = true;
    private boolean ativo = true;
    // private boolean validacao -> não sei ainda como vai funcionar a validação
    @Enumerated(EnumType.STRING)
    private Generos genero;
    @JsonIgnore
    @OneToMany(mappedBy = "user")
    private List<Avaliacao> avaliacoes;
    @JsonIgnore
    @OneToMany(mappedBy = "motorista")
    private List<Carona> minhasCaronas;
    @OneToMany(mappedBy = "passageiro")
    private List<Reserva> minhasReservas;
    @JsonIgnore
    @OneToOne(cascade = CascadeType.ALL)
    private Preferencias preferencia;
    @OneToMany(mappedBy = "dono")
    private List<Carro> meusCarros;
}
