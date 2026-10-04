package com.example.vaideboa.model;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Chat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @JsonIgnore
    @OneToMany(mappedBy = "chat")
    List<Mensagem> mensagens;
    // @JsonIgnore
    // @ManyToOne
    // @JoinColumn(name = "motorista_id")
    // private User motorista;
    // @JsonIgnore
    // @ManyToOne
    // @JoinColumn(name = "passageiro_id")
    // private User passageiro;
    @JsonIgnore 
    @OneToOne 
    @JoinColumn(name = "reserva_id", unique = true, nullable = false)
    private Reserva reserva;
    private LocalDateTime criadoEm;
} 
