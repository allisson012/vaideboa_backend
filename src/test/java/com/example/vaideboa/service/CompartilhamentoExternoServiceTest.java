package com.example.vaideboa.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.*;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.locationtech.jts.geom.*;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import com.example.vaideboa.model.*;
import com.example.vaideboa.model.enums.*;
import com.example.vaideboa.repository.*;

class CompartilhamentoExternoServiceTest {
    private final Instant agora = Instant.parse("2026-10-24T15:00:00Z");
    private final String token = "a".repeat(43);
    private CompartilhamentoPublicoRepository compartilhamentos;
    private CaronaRepository caronas;
    private ReservaRepository reservas;
    private UserRepository usuarios;
    private CompartilhamentoExternoService service;
    private Carona carona;
    private User motorista;
    private User passageiro;
    private CompartilhamentoPublico compartilhamento;
    private Reserva reserva;

    @BeforeEach
    void configurar() {
        compartilhamentos = mock(CompartilhamentoPublicoRepository.class);
        caronas = mock(CaronaRepository.class);
        reservas = mock(ReservaRepository.class);
        usuarios = mock(UserRepository.class);
        service = new CompartilhamentoExternoService(compartilhamentos, caronas, reservas, usuarios,
                Clock.fixed(agora, ZoneOffset.UTC), "https://exemplo.com/", Duration.ofHours(24), Duration.ofSeconds(30));
        motorista = new User();
        motorista.setId(1L);
        passageiro = new User();
        passageiro.setId(2L);
        carona = new Carona();
        carona.setId(3L);
        carona.setMotorista(motorista);
        carona.setStatusCarona(StatusCarona.EM_ANDAMENTO);
        compartilhamento = new CompartilhamentoPublico();
        compartilhamento.setCarona(carona);
        compartilhamento.setToken(token);
        compartilhamento.setCriadoEm(agora.minusSeconds(60));
        compartilhamento.setExpiraEm(agora.plusSeconds(3600));
        reserva = new Reserva();
        reserva.setAprovado(true);
        reserva.setStatusReserva(StatusReserva.ACEITA);
        when(usuarios.findByUsernameAndAtivoTrue("motorista")).thenReturn(Optional.of(motorista));
        when(usuarios.findByUsernameAndAtivoTrue("passageiro")).thenReturn(Optional.of(passageiro));
        when(caronas.buscarPorIdComBloqueio(3L)).thenReturn(Optional.of(carona));
        when(reservas.findByCaronaAndPassageiro(carona, passageiro)).thenReturn(Optional.of(reserva));
        when(compartilhamentos.findByCaronaId(3L)).thenReturn(Optional.of(compartilhamento));
    }

    @Test
    void motoristaEPassageiroRecebemMesmoLinkSemRenovarPrazo() {
        var linkMotorista = service.obterOuCriarLink(3L, "motorista");
        var linkPassageiro = service.obterOuCriarLink(3L, "passageiro");
        assertEquals("https://exemplo.com/v#" + token, linkMotorista.getUrl());
        assertEquals(linkMotorista, linkPassageiro);
        assertEquals(agora.plusSeconds(3600), linkMotorista.getExpiraEm());
        verify(compartilhamentos, never()).save(any());
    }

    @Test
    void criaTokenUmaVezComPosicaoInicialVazia() {
        when(compartilhamentos.findByCaronaId(3L)).thenReturn(Optional.empty());
        when(compartilhamentos.save(any())).thenAnswer(invocacao -> {
            CompartilhamentoPublico criado = invocacao.getArgument(0);
            when(compartilhamentos.findByCaronaId(3L)).thenReturn(Optional.of(criado));
            return criado;
        });
        var primeiro = service.obterOuCriarLink(3L, "motorista");
        assertEquals(primeiro, service.obterOuCriarLink(3L, "passageiro"));
        ArgumentCaptor<CompartilhamentoPublico> captor = ArgumentCaptor.forClass(CompartilhamentoPublico.class);
        verify(compartilhamentos).save(captor.capture());
        var criado = captor.getValue();
        assertTrue(criado.getToken().matches("[A-Za-z0-9_-]{43}"));
        assertEquals(agora, criado.getCriadoEm());
        assertEquals(agora.plus(Duration.ofHours(24)), criado.getExpiraEm());
        assertNull(criado.getUltimaPosicao());
        assertNull(criado.getUltimaLocalizacaoEm());
    }

    @ParameterizedTest
    @EnumSource(StatusReserva.class)
    void verificaCadaEstadoDaReserva(StatusReserva estado) {
        reserva.setStatusReserva(estado);
        if (estado == StatusReserva.ACEITA || estado == StatusReserva.EMBARQUE_LIBERADO || estado == StatusReserva.EMBARCADO) {
            assertNotNull(service.obterOuCriarLink(3L, "passageiro"));
        } else {
            assertStatus(HttpStatus.NOT_FOUND, () -> service.obterOuCriarLink(3L, "passageiro"));
            verifyNoInteractions(compartilhamentos);
        }
    }

    @Test
    void bloqueiaReservaNaoAprovada() {
        reserva.setAprovado(false);
        assertStatus(HttpStatus.NOT_FOUND, () -> service.obterOuCriarLink(3L, "passageiro"));
        verifyNoInteractions(compartilhamentos);
    }

    @Test
    void bloqueiaQuemNaoTemReserva() {
        when(reservas.findByCaronaAndPassageiro(carona, passageiro)).thenReturn(Optional.empty());
        assertStatus(HttpStatus.NOT_FOUND, () -> service.obterOuCriarLink(3L, "passageiro"));
    }

    @Test
    void bloqueiaUsuarioInativoOuAusente() {
        assertStatus(HttpStatus.NOT_FOUND, () -> service.obterOuCriarLink(3L, "inativo"));
        verifyNoInteractions(caronas, compartilhamentos);
    }

    @Test
    void bloqueiaCaronaInexistente() {
        when(caronas.buscarPorIdComBloqueio(3L)).thenReturn(Optional.empty());
        assertStatus(HttpStatus.NOT_FOUND, () -> service.obterOuCriarLink(3L, "motorista"));
    }

    @ParameterizedTest
    @EnumSource(value = StatusCarona.class, names = "EM_ANDAMENTO", mode = EnumSource.Mode.EXCLUDE)
    void bloqueiaLinkForaDeAndamento(StatusCarona estado) {
        carona.setStatusCarona(estado);
        assertStatus(HttpStatus.CONFLICT, () -> service.obterOuCriarLink(3L, "motorista"));
        verifyNoInteractions(compartilhamentos);
    }

    @Test
    void expiracaoExataNaoRegeneraToken() {
        compartilhamento.setExpiraEm(agora);
        assertStatus(HttpStatus.CONFLICT, () -> service.obterOuCriarLink(3L, "motorista"));
        verify(compartilhamentos, never()).save(any());
    }

    @Test
    void consultaSemPosicaoAguardaPrimeiraLocalizacao() {
        configurarProjecao(null, null);
        var resposta = service.consultar(token);
        assertEquals("AGUARDANDO_PRIMEIRA_LOCALIZACAO", resposta.getEstadoLocalizacao());
        assertNull(resposta.getLocalizacao());
        assertNull(resposta.getUltimaAtualizacaoEm());
    }

    @Test
    void consultaConvertePointSemInverterCoordenadas() {
        configurarProjecao(new GeometryFactory().createPoint(new Coordinate(-46.6, -23.5)), agora);
        var resposta = service.consultar(token);
        assertEquals("DISPONIVEL", resposta.getEstadoLocalizacao());
        assertEquals(-23.5, resposta.getLocalizacao().getLatitude());
        assertEquals(-46.6, resposta.getLocalizacao().getLongitude());
        assertEquals(agora, resposta.getUltimaAtualizacaoEm());
    }

    @Test
    void consideraDesatualizadaNoLimiteConfigurado() {
        configurarProjecao(new GeometryFactory().createPoint(new Coordinate(0, 0)), agora.minusSeconds(30));
        assertEquals("DESATUALIZADA", service.consultar(token).getEstadoLocalizacao());
    }

    @Test
    void consultaIndisponivelNaoRetornaCoordenadas() {
        assertStatus(HttpStatus.NOT_FOUND, () -> service.consultar(token));
        verify(compartilhamentos).buscarLocalizacaoValida(token, agora, StatusCarona.EM_ANDAMENTO);
    }

    @Test
    void tokenMalformadoNaoConsultaBanco() {
        assertStatus(HttpStatus.NOT_FOUND, () -> service.consultar("invalido"));
        verifyNoInteractions(compartilhamentos);
    }

    @Test
    void atualizaPointComSridEHorarioDoRecebimento() {
        service.atualizarLocalizacao(3L, -23.5, -46.6, agora);
        service.atualizarLocalizacao(3L, -23.5, -46.6, agora.plusSeconds(5));
        ArgumentCaptor<Point> ponto = ArgumentCaptor.forClass(Point.class);
        verify(compartilhamentos).atualizarLocalizacao(eq(3L), ponto.capture(), eq(agora), eq(StatusCarona.EM_ANDAMENTO));
        assertEquals(4326, ponto.getValue().getSRID());
        assertEquals(-46.6, ponto.getValue().getX());
        assertEquals(-23.5, ponto.getValue().getY());
        verify(compartilhamentos).atualizarLocalizacao(eq(3L), any(), eq(agora.plusSeconds(5)), eq(StatusCarona.EM_ANDAMENTO));
    }

    static Stream<double[]> coordenadasInvalidas() {
        return Stream.of(new double[]{Double.NaN, 0}, new double[]{0, Double.POSITIVE_INFINITY},
                new double[]{91, 0}, new double[]{-91, 0}, new double[]{0, 181}, new double[]{0, -181});
    }

    @ParameterizedTest
    @MethodSource("coordenadasInvalidas")
    void ignoraCoordenadasInvalidas(double[] coordenadas) {
        service.atualizarLocalizacao(3L, coordenadas[0], coordenadas[1], agora);
        verifyNoInteractions(compartilhamentos);
    }

    private void configurarProjecao(Point ponto, Instant recebidaEm) {
        var projecao = mock(CompartilhamentoPublicoRepository.LocalizacaoPublica.class);
        when(projecao.getUltimaPosicao()).thenReturn(ponto);
        when(projecao.getUltimaLocalizacaoEm()).thenReturn(recebidaEm);
        when(compartilhamentos.buscarLocalizacaoValida(token, agora, StatusCarona.EM_ANDAMENTO))
                .thenReturn(Optional.of(projecao));
    }

    private void assertStatus(HttpStatus status, Runnable acao) {
        var ex = assertThrows(ResponseStatusException.class, acao::run);
        assertEquals(status, ex.getStatusCode());
    }
}
