package com.example.vaideboa.repository;

import java.util.List;
import java.util.Optional;

import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.example.vaideboa.model.Rota;

@Repository
public interface RotaRepository extends JpaRepository<Rota,Long>{

// Busca so levando em consideração o destino
@Query(value = """
SELECT *
FROM rota r
WHERE ST_DWithin(
    r.destino::geography,
    ST_SetSRID(:point, 4326)::geography,
    10000
)
""", nativeQuery = true)
Optional<List<Rota>> buscarCaronasPorDestino(@Param("point") Point point);

// Busca levando em consideração a saida e o destino e em um raio de até 10 km para ambos
@Query(value = """
SELECT *
FROM rota r
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
""", nativeQuery = true)
Optional<List<Rota>> buscarCaronas(@Param("saida") Point saida, @Param("destino") Point destino);

 }
