package com.example.vaideboa.model;

import java.util.List;

import org.locationtech.jts.geom.Point;

import com.example.vaideboa.model.enums.StatusReserva;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "reserva")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reserva {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User passageiro;
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "carona_id")
    private Carona carona;
    private boolean aprovado; // aprovado o pagamento
    @JsonIgnore
    @OneToMany(mappedBy = "reserva")
    private List<Avaliacao> avaliacoes;
    @Column(columnDefinition = "geometry(Point,4326)")
    private Point saida;
    @Column(columnDefinition = "geometry(Point,4326)")
    private Point destino;
    // pagamento
    private boolean jaEnviadoCodigoInicio;
    private boolean jaEnviadoCodigoFim;
    @OneToOne(mappedBy = "reserva", cascade = CascadeType.ALL)
    private ReservaCodigo reservaCodigo;
    @Enumerated(EnumType.STRING)
    private StatusReserva statusReserva;
    @JsonIgnore
    @OneToMany(mappedBy = "reserva", cascade = CascadeType.ALL)
    private List<ReservaEvento> eventos;
}
