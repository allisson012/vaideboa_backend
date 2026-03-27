package com.example.vaideboa.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.vaideboa.model.Reserva;

public interface ReservaRepository extends JpaRepository<Reserva,Long>{
    
}
