package com.example.vaideboa.model;

import java.sql.Timestamp;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "reserva_codigo")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReservaCodigo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "reserva_id")
    private Reserva reserva;

    private int codigoEmbarque;
    private boolean embarqueLiberado = false; // para o sistema saber se ele ja liberou o codigo
    private boolean embarqueRealizado = false; // para quando o passageiro colocar o codigo

    private int codigoDesembarque;
    private boolean desembarqueLiberado = false;
    private boolean desembarqueRealizado = false;

    private Timestamp criadoEm;

}
