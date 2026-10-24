package com.example.vaideboa.repository;

import java.time.Instant;
import java.util.Optional;
import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import com.example.vaideboa.model.CompartilhamentoPublico;
import com.example.vaideboa.model.enums.StatusCarona;

public interface CompartilhamentoPublicoRepository extends JpaRepository<CompartilhamentoPublico, Long> {
    Optional<CompartilhamentoPublico> findByCaronaId(Long idCarona);

    @Query("""
        select c.ultimaPosicao as ultimaPosicao, c.ultimaLocalizacaoEm as ultimaLocalizacaoEm
        from CompartilhamentoPublico c
        where c.token = :token and c.expiraEm > :agora
        and c.carona.statusCarona = :status
        """)
    Optional<LocalizacaoPublica> buscarLocalizacaoValida(@Param("token") String token, @Param("agora") Instant agora, @Param("status") StatusCarona status);

    @Modifying
    @Query("""
        update CompartilhamentoPublico c
        set c.ultimaPosicao = :posicao, c.ultimaLocalizacaoEm = :recebidaEm
        where c.carona.id = :idCarona and c.expiraEm > :recebidaEm
        and (c.ultimaLocalizacaoEm is null or c.ultimaLocalizacaoEm < :recebidaEm)
        and c.carona.id in (select carona.id from Carona carona where carona.statusCarona = :status)
        """)
    int atualizarLocalizacao(@Param("idCarona") Long idCarona, @Param("posicao") Point posicao, @Param("recebidaEm") Instant recebidaEm, @Param("status") StatusCarona status);

    interface LocalizacaoPublica {
        Point getUltimaPosicao();
        Instant getUltimaLocalizacaoEm();
    }
}
