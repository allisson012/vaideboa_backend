package com.example.vaideboa.model;

import com.example.vaideboa.model.enums.StatusPedido;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "pedido_carona")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PedidoCarona {
  // classe vai servir como um intermediario aonde quando um usuário quiser agendar uma carona 
  // vai ser criado esse Pedido para que o motorista decida se quer aceitar ou não se aceitar é 
  // criado a reserva
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "passageiro_id")
    private User passageiro;
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "carona_id")
    private Carona carona;
    @Enumerated(EnumType.STRING)
    private StatusPedido status;
}
