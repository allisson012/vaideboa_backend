package com.example.vaideboa.repository;

import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import com.example.vaideboa.model.Carona;
import com.example.vaideboa.model.Reserva;
import com.example.vaideboa.model.User;
import com.example.vaideboa.model.enums.StatusReserva;

public interface ReservaRepository extends JpaRepository<Reserva,Long>{
    @Query("select r.carona.id from Reserva r where r.id = :id")
    Optional<Long> buscarIdCarona(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Reserva r where r.id = :id")
    Optional<Reserva> buscarPorIdComBloqueio(@Param("id") Long id);

    Optional<Reserva> findByCaronaAndPassageiro(Carona carona, User passageiro);

    List<Reserva> findByCaronaAndStatusReserva(Carona carona, StatusReserva statusReserva);
}
