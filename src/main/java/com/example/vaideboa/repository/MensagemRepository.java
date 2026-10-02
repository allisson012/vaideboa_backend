package com.example.vaideboa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.vaideboa.model.Mensagem;

@Repository 
public interface MensagemRepository extends JpaRepository<Mensagem, Long> {
    
}
