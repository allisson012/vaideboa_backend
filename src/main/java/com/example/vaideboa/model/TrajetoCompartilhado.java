package com.example.vaideboa.model;

import java.time.LocalDateTime;

import org.locationtech.jts.geom.LineString;

import com.example.vaideboa.model.enums.StatusCompartilhamento;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "trajeto_compartilhado")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TrajetoCompartilhado {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    // tem que ver se vai ter somente um compartilhamento ou se o motorista pode cancelar e iniciar outro
    @OneToOne(mappedBy = "trajetoCompartilhado")
    @JsonIgnore
    private Carona carona;
    private LocalDateTime inicio;
    private LocalDateTime fim;
    @Column(columnDefinition = "geometry(LineString,4326)")
    private LineString trajeto;
    private Double distancia_percorrida;
    @Enumerated(EnumType.STRING)
    private StatusCompartilhamento statusCompartilhamento;
}
