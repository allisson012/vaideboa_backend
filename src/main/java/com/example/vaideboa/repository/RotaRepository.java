package com.example.vaideboa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.vaideboa.model.Rota;

@Repository
public interface RotaRepository extends JpaRepository<Rota,Long>{
    
}
