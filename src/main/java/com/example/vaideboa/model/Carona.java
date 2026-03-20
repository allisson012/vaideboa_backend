package com.example.vaideboa.model;

import java.time.LocalDateTime;
import java.util.List;

import org.locationtech.jts.geom.Point;

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
@Table(name = "carona")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Carona {
   @Id
   @GeneratedValue(strategy= GenerationType.IDENTITY)
   private Long id; 
   private int qntAssentos;
   private LocalDateTime dataHora;
   @Column(columnDefinition = "geometry(Point,4326)")
   private Point saida;
   @Column(columnDefinition = "geometry(Point,4326)")
   private Point destino;
   @OneToMany(mappedBy = "carona")
   private List<Avaliacao> avaliacoes;
}
