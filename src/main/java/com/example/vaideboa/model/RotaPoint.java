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
public class RotaPoint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private int indexOrder;
    
    @ManyToOne
    @JoinColumn(name = "rota_id")
    @JsonIgnore
    private Rota rota;
    @Column(columnDefinition = "geometry(Point,4326)")
    private Point localizacao;

    /*
        @Column(columnDefinition = "geometry(LineString,4326)")
        private LineString geometria;

        private Double distancia;

        private Double duracao;

        
    */
}
