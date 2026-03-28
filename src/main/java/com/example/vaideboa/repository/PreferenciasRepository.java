package com.example.vaideboa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.vaideboa.model.Preferencias;

@Repository
public interface PreferenciasRepository extends JpaRepository<Preferencias,Long>{
    
}
