package com.example.vaideboa.model;

import org.locationtech.jts.geom.Point;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "faculdade")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Faculdade {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(columnDefinition = "geometry(Point,4326)")
    private Point Localizacao;
    private String nome;
    // private Endereco endereco -> para exibir de um jeito facil com string

    //filtrar caronas da mesma universidade
    //mostrar "caronas da sua universidade"
    //restringir acesso (ex: só de alunos dessa universidade)
    // usuario poder salvar como tipo uma casa
    // parceria com a faculdade para que alunos daquela faculdade ganhem 
    // verificado e entre com email e entrem com email educacional
}
