package com.example.vaideboa.model;

import java.util.List;

import org.locationtech.jts.geom.Point;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
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
    private Point saida; // saida do passageiro em si vou usar na hora de gerar os codigos de confirmação
    private Point destino;
    // pagamento
    private boolean jaEnviadoCodigoInicio;
    private boolean jaEnviadoCodigoFim;
}
