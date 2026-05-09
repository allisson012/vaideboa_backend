package com.example.vaideboa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.vaideboa.model.ReservaCodigo;

@Repository
public interface ReservaCodigoRepository extends JpaRepository<ReservaCodigo, Long> {
    
}
