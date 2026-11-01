package com.example.vaideboa.model;

import com.example.vaideboa.model.enums.NivelPreferencia;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "preferencias")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Preferencias {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING)
    private NivelPreferencia conversa;
    @Enumerated(EnumType.STRING)
    private NivelPreferencia musica;
    @Enumerated(EnumType.STRING)
    private NivelPreferencia cigarro;
    @Enumerated(EnumType.STRING)
    private NivelPreferencia animais;
    private Long raioDeslocamentoMetros = 2000L;
    @JsonIgnore
    @OneToOne
    private User user;
}
