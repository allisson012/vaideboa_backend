package com.example.vaideboa.model;

import java.sql.Timestamp;

import com.example.vaideboa.model.enums.TipoEvento;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReservaEvento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private Reserva reserva;
    @Enumerated(EnumType.STRING)
    private TipoEvento tipoEvento;
    private Timestamp criadoEm;
    // classe criado para servir como um log da corrida em si para não perder informação 
    // sobre cada passageiro dentro da corrida
}
