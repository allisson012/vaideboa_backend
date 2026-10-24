package com.example.vaideboa.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.time.*;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import com.example.vaideboa.model.*;
import com.example.vaideboa.model.enums.*;
import com.example.vaideboa.repository.*;

class CompartilhamentoServiceTest {
    private final Instant agora = Instant.parse("2026-10-24T15:00:00Z");
    private CompartilhamentoExternoService externo;
    private SimpMessagingTemplate mensagens;
    private CaronaRepository caronas;
    private TrajetoCompartilhadoRepository trajetos;
    private CompartilhamentoService service;
    private TrajetoCompartilhado trajeto;
    private Carona carona;

    @BeforeEach
    void configurar() {
        externo = mock(CompartilhamentoExternoService.class);
        mensagens = mock(SimpMessagingTemplate.class);
        caronas = mock(CaronaRepository.class);
        trajetos = mock(TrajetoCompartilhadoRepository.class);
        service = new CompartilhamentoService(mensagens, externo, Clock.fixed(agora, ZoneOffset.UTC), caronas, trajetos);
        User motorista = new User();
        motorista.setUsername("motorista");
        carona = new Carona();
        carona.setId(1L);
        carona.setMotorista(motorista);
        carona.setStatusCarona(StatusCarona.EM_ANDAMENTO);
        trajeto = new TrajetoCompartilhado();
        trajeto.setStatusCompartilhamento(StatusCompartilhamento.EM_ANDAMENTO);
        when(caronas.findById(1L)).thenReturn(Optional.of(carona));
        when(trajetos.findByCarona(carona)).thenReturn(Optional.of(trajeto));
    }

    @Test
    void coordenadaRepetidaAtualizaPublicoSemAumentarHistorico() {
        service.atualizarLocalizacao(1L, -23, -46, "motorista");
        service.atualizarLocalizacao(1L, -23, -46, "motorista");
        verify(externo, times(2)).atualizarLocalizacao(1L, -23, -46, agora);
        verify(trajetos, times(1)).save(trajeto);
    }

    @Test
    void falhaPublicaNaoInterrompeTransmissaoEPersistenciaInternas() {
        doThrow(new IllegalStateException("falha simulada")).when(externo).atualizarLocalizacao(1L, -23, -46, agora);
        assertDoesNotThrow(() -> service.atualizarLocalizacao(1L, -23, -46, "motorista"));
        verify(mensagens).convertAndSend(eq("/topic/carona/1"), any(Object.class));
        verify(trajetos).save(trajeto);
    }

    @Test
    void semTrajetoAtivoRetornaListaVazia() {
        when(trajetos.findByCarona(carona)).thenReturn(Optional.empty());
        assertTrue(service.obterTrajetoAtual(1L).isEmpty());
    }

    @Test
    void outroUsuarioNaoAtualizaLocalizacaoPublica() {
        service.atualizarLocalizacao(1L, -23, -46, "outro");
        verifyNoInteractions(externo, mensagens);
    }

    @Test
    void caronaEncerradaNaoAtualizaLocalizacaoPublica() {
        carona.setStatusCarona(StatusCarona.CONCLUIDA);
        service.atualizarLocalizacao(1L, -23, -46, "motorista");
        verifyNoInteractions(externo, mensagens);
    }

    @Test
    void coordenadaInvalidaNaoEhTransmitidaNemPersistida() {
        service.atualizarLocalizacao(1L, Double.NaN, 0, "motorista");
        verifyNoInteractions(caronas, externo, mensagens, trajetos);
    }
}
