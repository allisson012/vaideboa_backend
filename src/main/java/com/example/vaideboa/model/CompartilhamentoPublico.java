package com.example.vaideboa.model;

import java.time.Instant;
import org.locationtech.jts.geom.Point;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "compartilhamento_publico")
@Data
@NoArgsConstructor
public class CompartilhamentoPublico {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "carona_id", nullable = false, unique = true)
    private Carona carona;

    @JsonIgnore
    @Column(nullable = false, unique = true, length = 43)
    private String token;

    @Column(nullable = false)
    private Instant criadoEm;
    @Column(nullable = false)
    private Instant expiraEm;
    @Column(columnDefinition = "geometry(Point,4326)")
    private Point ultimaPosicao;
    private Instant ultimaLocalizacaoEm;
}
