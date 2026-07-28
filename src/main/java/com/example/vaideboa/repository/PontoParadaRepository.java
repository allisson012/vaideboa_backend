package com.example.vaideboa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.vaideboa.model.PontoParada;

@Repository
public interface PontoParadaRepository extends JpaRepository<PontoParada, Long>{
    
}
