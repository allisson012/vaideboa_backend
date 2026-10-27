
package com.example.vaideboa.service;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.EnumSet;

import org.locationtech.jts.geom.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.vaideboa.Dtos.AcompanhamentoPublicoDto;
import com.example.vaideboa.Dtos.CompartilhamentoCaronaDto;
import com.example.vaideboa.model.*;
import com.example.vaideboa.model.enums.*;
import com.example.vaideboa.repository.*;

@Service
public class CompartilhamentoExternoService {
    private final CompartilhamentoPublicoRepository compartilhamentos;
    private final CaronaRepository caronas;
    private final ReservaRepository reservas;
    private final UserRepository usuarios;
    private final Clock clock;
    private final String urlBase;
    private final Duration validade;
    private final Duration desatualizacao;
    private final SecureRandom random = new SecureRandom();
    private static final EnumSet<StatusReserva> ESTADOS_PERMITIDOS = EnumSet.of(
            StatusReserva.ACEITA, StatusReserva.EMBARQUE_LIBERADO, StatusReserva.EMBARCADO);

    public CompartilhamentoExternoService(CompartilhamentoPublicoRepository compartilhamentos,
            CaronaRepository caronas, ReservaRepository reservas, UserRepository usuarios, Clock clock,
            @Value("${compartilhamento.publico.url-base}") String urlBase,
            @Value("${compartilhamento.publico.validade:PT24H}") Duration validade,
            @Value("${compartilhamento.publico.desatualizacao:PT30S}") Duration desatualizacao) {
        if (validade.isNegative() || validade.isZero() || desatualizacao.isNegative() || desatualizacao.isZero()) {
            throw new IllegalArgumentException("Os prazos do compartilhamento devem ser positivos");
        }
        this.compartilhamentos = compartilhamentos;
        this.caronas = caronas;
        this.reservas = reservas;
        this.usuarios = usuarios;
        this.clock = clock;
        this.urlBase = urlBase.replaceAll("/+$", "");
        this.validade = validade;
        this.desatualizacao = desatualizacao;
    }

    @Transactional
    public CompartilhamentoCaronaDto obterOuCriarLink(Long idCarona, String username) {
        User user = usuarios.findByUsernameAndAtivoTrue(username).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        Carona carona = caronas.buscarPorIdComBloqueio(idCarona).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!carona.getMotorista().getId().equals(user.getId())) {
            boolean permitido = reservas.findByCaronaAndPassageiro(carona, user).filter(reserva -> reserva.isAprovado() && ESTADOS_PERMITIDOS.contains(reserva.getStatusReserva())).isPresent();
            if (!permitido) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);
            }
        }
        if (carona.getStatusCarona() != StatusCarona.EM_ANDAMENTO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }
        Instant agora = clock.instant();
        CompartilhamentoPublico compartilhamento = compartilhamentos.findByCaronaId(idCarona).orElseGet(() -> criarCompartilhamento(carona, agora));
        if (!compartilhamento.getExpiraEm().isAfter(agora)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }
        return new CompartilhamentoCaronaDto(urlBase + "/v#" + compartilhamento.getToken(),compartilhamento.getExpiraEm());
    }

    private CompartilhamentoPublico criarCompartilhamento(Carona carona, Instant agora) {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        CompartilhamentoPublico compartilhamento = new CompartilhamentoPublico();
        compartilhamento.setCarona(carona);
        compartilhamento.setToken(Base64.getUrlEncoder().withoutPadding().encodeToString(bytes));
        compartilhamento.setCriadoEm(agora);
        compartilhamento.setExpiraEm(agora.plus(validade));
        return compartilhamentos.save(compartilhamento);
    }

    @Transactional(readOnly = true)
    public AcompanhamentoPublicoDto consultar(String token) {
        if (token == null || !token.matches("[A-Za-z0-9_-]{43}")) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        Instant agora = clock.instant();
        var localizacao = compartilhamentos.buscarLocalizacaoValida(token, agora, StatusCarona.EM_ANDAMENTO).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        Point posicao = localizacao.getUltimaPosicao();
        Instant recebidaEm = localizacao.getUltimaLocalizacaoEm();
        if (posicao == null || recebidaEm == null) {
            return new AcompanhamentoPublicoDto("EM_ANDAMENTO", "AGUARDANDO_PRIMEIRA_LOCALIZACAO", null, null);
        }
        String estado = !recebidaEm.plus(desatualizacao).isAfter(agora) ? "DESATUALIZADA" : "DISPONIVEL";
        return new AcompanhamentoPublicoDto("EM_ANDAMENTO", estado, new AcompanhamentoPublicoDto.Coordenadas(posicao.getY(), posicao.getX()), recebidaEm);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void atualizarLocalizacao(Long idCarona, double latitude, double longitude, Instant recebidaEm) {
        if (!coordenadasValidas(latitude, longitude)) {
            return;
        }
        Point posicao = new GeometryFactory(new PrecisionModel(), 4326).createPoint(new Coordinate(longitude, latitude));
        compartilhamentos.atualizarLocalizacao(idCarona, posicao, recebidaEm, StatusCarona.EM_ANDAMENTO);
    }

    public static boolean coordenadasValidas(double latitude, double longitude) {
        return Double.isFinite(latitude) && Double.isFinite(longitude) && latitude >= -90 && latitude <= 90 && longitude >= -180 && longitude <= 180;
    }
}
