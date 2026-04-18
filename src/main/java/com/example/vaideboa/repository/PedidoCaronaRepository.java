package com.example.vaideboa.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.vaideboa.model.Carona;
import com.example.vaideboa.model.PedidoCarona;
import com.example.vaideboa.model.User;
import com.example.vaideboa.model.enums.StatusPedido;

@Repository
public interface PedidoCaronaRepository extends JpaRepository<PedidoCarona,Long>{
    boolean existsByPassageiroAndCarona(User user, Carona carona);

    List<PedidoCarona> findByCaronaAndStatus(Carona carona, StatusPedido status);
}
