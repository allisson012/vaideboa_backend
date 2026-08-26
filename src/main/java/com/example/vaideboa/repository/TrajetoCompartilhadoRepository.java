package com.example.vaideboa.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.vaideboa.model.Carona;
import com.example.vaideboa.model.TrajetoCompartilhado;

@Repository
public interface TrajetoCompartilhadoRepository extends JpaRepository<TrajetoCompartilhado, Long> {
    Optional<TrajetoCompartilhado> findByCarona(Carona carona);
}
