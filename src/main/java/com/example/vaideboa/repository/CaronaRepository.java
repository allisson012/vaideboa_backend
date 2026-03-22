package com.example.vaideboa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.vaideboa.model.Carona;

@Repository
public interface CaronaRepository extends JpaRepository<Carona,Long>{
    
}
