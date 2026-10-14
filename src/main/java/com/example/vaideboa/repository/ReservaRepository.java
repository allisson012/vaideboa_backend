package com.example.vaideboa.repository;

import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.vaideboa.model.Carona;
import com.example.vaideboa.model.Reserva;
import com.example.vaideboa.model.User;
import com.example.vaideboa.model.enums.StatusReserva;

public interface ReservaRepository extends JpaRepository<Reserva,Long>{
    Optional<Reserva> findByCaronaAndPassageiro(Carona carona, User passageiro);

    List<Reserva> findByCaronaAndStatusReserva(Carona carona, StatusReserva statusReserva);
}
