package com.example.vaideboa.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.vaideboa.model.Carona;
import com.example.vaideboa.model.Reserva;
import com.example.vaideboa.model.User;

public interface ReservaRepository extends JpaRepository<Reserva,Long>{
    Optional<Reserva> findByCaronaAndPassageiro(Carona carona, User passageiro);
}
