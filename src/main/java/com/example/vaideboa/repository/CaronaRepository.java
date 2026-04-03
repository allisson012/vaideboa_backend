package com.example.vaideboa.repository;

import java.time.LocalDate;
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

    @Query(value = """
    SELECT c.*
    FROM carona c
    JOIN rota r ON r.id = c.rota_id
    WHERE ST_DWithin(
        r.saida::geography,
        ST_SetSRID(:saida, 4326)::geography,
        10000
    )
    AND ST_DWithin(
        r.destino::geography,
        ST_SetSRID(:destino, 4326)::geography,
        10000
    )
    AND c.data = :data
    """, nativeQuery = true)
    List<Carona> buscarCaronas(
        @Param("saida") Point saida,
        @Param("destino") Point destino,
        @Param("data") LocalDate data
    );

    // Buscar caronas em todo o trajeto
    @Query(value = """
    SELECT c.*
    FROM carona c
    JOIN rota r ON r.id = c.rota_id
    WHERE ST_DWithin(
        r.trajeto::geography,
        ST_SetSRID(:saida, 4326)::geography,
        10000
    )
    AND ST_DWithin(
        r.trajeto::geography,
        ST_SetSRID(:destino, 4326)::geography,
        10000
    )
    AND c.data = :data
    """, nativeQuery = true)
    List<Carona> buscarCaronasTodoTrajeto(
        @Param("saida") Point saida,
        @Param("destino") Point destino,
        @Param("data") LocalDate data
    );
}
