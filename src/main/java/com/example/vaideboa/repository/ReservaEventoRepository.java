package com.example.vaideboa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.vaideboa.model.ReservaEvento;

@Repository
public interface ReservaEventoRepository extends JpaRepository<ReservaEvento,Long>{
    
}
