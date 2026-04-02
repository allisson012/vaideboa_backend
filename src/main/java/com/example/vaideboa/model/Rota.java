package com.example.vaideboa.model;

import java.util.List;

import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Rota {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne(mappedBy = "rota")
    @JsonIgnore
    private Carona carona;
    @Column(columnDefinition = "geometry(Point,4326)")
    private Point saida;
    @Column(columnDefinition = "geometry(Point,4326)")
    private Point destino;
    @OneToMany(mappedBy = "rota")
    @JsonIgnore
    private List<RotaPoint> rota_points;
    @Column(columnDefinition = "geometry(LineString,4326)")
    private LineString trajeto;
    // @Column(columnDefinition = "jsonb")
    // private String geojson;
    private Double distancia;
    private Double duracao;
}
