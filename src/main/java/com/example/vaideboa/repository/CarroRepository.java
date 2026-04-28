package com.example.vaideboa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.vaideboa.model.Carro;

@Repository
public interface CarroRepository extends JpaRepository<Carro,Long>{
    boolean existsByPlaca(String placa);
}
