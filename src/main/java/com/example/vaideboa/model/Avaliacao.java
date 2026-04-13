package com.example.vaideboa.model;

import com.example.vaideboa.model.enums.TipoAvaliacao;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "avaliacao")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Avaliacao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "avaliador_id")
    @JsonIgnore
    private User avaliador;
    @ManyToOne
    @JoinColumn(name = "avaliado_id")
    @JsonIgnore
    private User avaliado;
    
    @ManyToOne
    private Reserva reserva;

    // definir o papel do user nessa avaliação ex motorista ou passageiro
    private Integer nota; // 4.5 1.0 null
    private String comentario;
    @Enumerated(EnumType.STRING)
    private TipoAvaliacao tipo;
}
