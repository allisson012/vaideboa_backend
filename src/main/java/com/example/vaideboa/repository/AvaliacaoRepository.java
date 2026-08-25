package com.example.vaideboa.repository;

import java.util.ArrayList;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.vaideboa.model.Avaliacao;
import com.example.vaideboa.model.Reserva;
import com.example.vaideboa.model.User;

@Repository
public interface AvaliacaoRepository extends JpaRepository<Avaliacao,Long> {
    Optional<Avaliacao> findByReservaAndAvaliadoAndAvaliador(Reserva reserva, User avaliado, User avaliador);
    Optional<ArrayList<Avaliacao>> findByAvaliadoId(Long id);
}
