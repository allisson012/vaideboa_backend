package com.example.vaideboa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.vaideboa.model.Carona;
import com.example.vaideboa.model.PedidoCarona;
import com.example.vaideboa.model.User;

@Repository
public interface PedidoCaronaRepository extends JpaRepository<PedidoCarona,Long>{
    boolean existsByPassageiroAndCarona(User user, Carona carona);
}
