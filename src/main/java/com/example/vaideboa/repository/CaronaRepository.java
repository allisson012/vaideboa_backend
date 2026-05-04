package com.example.vaideboa.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.vaideboa.Dtos.CaronaEmAndamentoDTO;
import com.example.vaideboa.model.Carona;
import com.example.vaideboa.model.Rota;
import com.example.vaideboa.model.User;

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


    List<Carona> findByMotoristaAndDataGreaterThanEqual(User motorista, LocalDate data);

    @Query(value = """
    SELECT c.*
    FROM carona c
    JOIN rota r ON r.id = c.rota_id
    WHERE 
    ST_DWithin(
        r.trajeto::geography,
        ST_SetSRID(:saida, 4326)::geography,
        10000
    )
    AND
    ST_DWithin(
        r.trajeto::geography,
        ST_SetSRID(:destino, 4326)::geography,
        10000
    )
    AND
    ST_LineLocatePoint(
        r.trajeto,
        ST_SetSRID(:saida, 4326)
    ) 
    <
    ST_LineLocatePoint(
        r.trajeto,
        ST_SetSRID(:destino, 4326)
    )

    AND c.data = :data
    """, nativeQuery = true)
    List<Carona> buscarCaronasTodoTrajeto(
        @Param("saida") Point saida,
        @Param("destino") Point destino,
        @Param("data") LocalDate data
    );

    List<Carona> findByDataAndHoraBetweenAndNotificado1hFalse(
    LocalDate data,
    LocalTime inicio,
    LocalTime fim
    );
    
/*  query para pegar passageiros para enviar o codigo de confirmação baseado na posição do motorista
 a ideia é se ele estiver a 300 m ou do ponto de saida ou destino de um passageiro enviar o 
 respectivo codigo para esse passageiro */
@Query(value = """
    WITH posicao AS (
        SELECT ST_GeomFromText(CAST(:posicaoMotorista AS text), 4326)::geography AS p
    )
    SELECT 
        u.id        AS userId,
        u.nome      AS userNome,
        r.id        AS reservaId,
        ST_AsText(r.saida)    AS saida,
        ST_AsText(r.destino)  AS destino,
        CASE 
            WHEN ST_DWithin(p.p, r.saida::geography, 300) THEN 'EMBARQUE'
            WHEN ST_DWithin(p.p, r.destino::geography, 300) THEN 'DESEMBARQUE'
        END AS tipo
    FROM posicao p
    JOIN carona c ON true
    JOIN users u    ON u.id = c.motorista_id
    JOIN rota ro    ON ro.id = c.rota_id
    JOIN reserva r  ON r.carona_id = c.id
    WHERE c.status_carona = 'EM_ANDAMENTO'
    AND r.aprovado = true
    AND (
        (ST_DWithin(p.p, r.saida::geography, 300) AND r.ja_enviado_codigo_inicio = false)
        OR
        (ST_DWithin(p.p, r.destino::geography, 300) AND r.ja_enviado_codigo_fim = false)
    )
""", nativeQuery = true)
List<CaronaEmAndamentoDTO> buscarProximosParaEnviarCodigo(
    @Param("posicaoMotorista") Point posicao
);

}
