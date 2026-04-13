package com.example.vaideboa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.vaideboa.model.Avaliacao;

@Repository
public interface AvaliacaoRepository extends JpaRepository<Avaliacao,Long> {
    
}
