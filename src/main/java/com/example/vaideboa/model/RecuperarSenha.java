package com.example.vaideboa.model;

import java.time.LocalDateTime;

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
public class RecuperarSenha {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String tokenHash;
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    private LocalDateTime expiresAt;
    private Boolean utilizado = false;
    private int tentativasFalhas = 0;
    // Impede o reenvio até este horário; nulo significa ausência de bloqueio temporário.
    private LocalDateTime bloqueadoAte;
    // Invalida o código atual, inclusive depois do fim do bloqueio temporário.
    private Boolean bloqueado = false;
}
