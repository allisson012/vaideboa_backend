package com.example.vaideboa.repository;

import java.util.List;
import java.util.Optional;

import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.vaideboa.model.Carona;
import com.example.vaideboa.model.Rota;

@Repository
public interface CaronaRepository extends JpaRepository<Carona,Long>{
    Optional<List<Carona>> findByRota(Rota rota);
}
