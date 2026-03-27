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
        
// @Query(value = """
// SELECT *
// FROM rota r
// WHERE ST_DWithin(
//     r.destino::geography,
//     ST_SetSRID(:point, 4326)::geography,
//     2000
// )
// """, nativeQuery = true)
// Optional<List<Rota>> buscarCaronas(@Param("point") Point point);

@Query(value = """
SELECT *
FROM rota r
WHERE ST_DWithin(
    r.destino::geography,
    ST_SetSRID(:point, 4326)::geography,
    10000
)
""", nativeQuery = true)
Optional<List<Rota>> buscarCaronas(@Param("point") Point point);

}
