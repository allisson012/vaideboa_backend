package com.example.vaideboa.model;

import org.locationtech.jts.geom.Point;
import jakarta.persistence.Column;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PontoParada {
    // se o ponto for distante da minha rota vou ter remodelar ela
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private int indexOrder;
    
    @ManyToOne
    @JoinColumn(name = "rota_id")
    @JsonIgnore
    private Rota rota;
    @Column(columnDefinition = "geometry(Point,4326)")
    private Point localizacao;
    private String textoPonto;
    // colocar uma string com o nome do ponto
    /*
        @Column(columnDefinition = "geometry(LineString,4326)")
        private LineString geometria;

        private Double distancia;

        private Double duracao;

        
    */
}
