package com.example.vaideboa.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;

import com.example.vaideboa.model.Carona;
import com.example.vaideboa.model.PedidoCarona;
import com.example.vaideboa.model.User;
import com.example.vaideboa.model.enums.StatusPedido;

@Repository
public interface PedidoCaronaRepository extends JpaRepository<PedidoCarona,Long>{
    @Query("select p.carona.id from PedidoCarona p where p.id = :id")
    Optional<Long> buscarIdCarona(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from PedidoCarona p where p.id = :id")
    Optional<PedidoCarona> buscarPorIdComBloqueio(@Param("id") Long id);

    boolean existsByPassageiroAndCarona(User user, Carona carona);

    List<PedidoCarona> findByCaronaAndStatus(Carona carona, StatusPedido status);

    List<PedidoCarona> findByPassageiroAndStatus(User passageiro, StatusPedido status);

    List<PedidoCarona> findByCaronaId(Long idCarona);
}
