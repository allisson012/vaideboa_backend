package com.example.vaideboa.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import com.example.vaideboa.model.enums.StatusCarona;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
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
@Table(name = "carona")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Carona {
   @Id
   @GeneratedValue(strategy= GenerationType.IDENTITY)
   private Long id; 
   private int qntAssentos;
   private int vagasDisponiveis;
   private LocalDate data;
   private LocalTime hora;
   @JsonIgnore
   @ManyToOne
   @JoinColumn(name = "user_id")
   private User motorista; // criador da carona
   // gerar recibo talvez fique na parte de pagamento
   @OneToOne(cascade = CascadeType.ALL)
   @JoinColumn(name = "rota_id")
   @JsonIgnore
   private Rota rota;
   private boolean realizado; // posso tirar esse campo pq já tem no statusCarona
   @JsonIgnore
   @OneToMany(mappedBy = "carona")
   private List<Reserva> reservas; 
   @JsonIgnore
   @ManyToOne
   @JoinColumn(name = "carro_id")
   private Carro carro;
   private Boolean notificado1h = false;
   private Boolean notificado30min = false;
   @Enumerated(EnumType.STRING)
   private StatusCarona statusCarona;
   @OneToOne(cascade = CascadeType.ALL)
   @JoinColumn(name = "trajeto_compartilhado_id")
   @JsonIgnore
   private TrajetoCompartilhado trajetoCompartilhado;
}
