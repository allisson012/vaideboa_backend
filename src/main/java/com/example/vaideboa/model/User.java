package com.example.vaideboa.model;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
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
    private boolean contaNaoExpirada = true;
    private boolean contaNaoBloqueada = true;
    private boolean credenciaisNaoExpiradas = true;
    private boolean ativo = true;
    // private boolean validacao
    private String genero;
    @OneToMany(mappedBy = "user")
    private List<Avaliacao> avaliacoes;

}
