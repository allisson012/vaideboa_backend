package com.example.vaideboa.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.vaideboa.model.Carro;
import com.example.vaideboa.model.User;

@Repository
public interface CarroRepository extends JpaRepository<Carro,Long>{
    boolean existsByPlaca(String placa);
    List<Carro> findByDonoAndAtivoTrue(User dono);
}
