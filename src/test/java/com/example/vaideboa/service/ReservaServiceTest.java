package com.example.vaideboa.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.example.vaideboa.model.*;
import com.example.vaideboa.model.enums.*;
import com.example.vaideboa.repository.*;

class ReservaServiceTest {
    private UserRepository usuarios;
    private ReservaRepository reservas;
    private CaronaRepository caronas;
    private ReservaService service;
    private Carona carona;
    private Reserva reserva;

    @BeforeEach
    void configurar() {
        usuarios = mock(UserRepository.class);
        reservas = mock(ReservaRepository.class);
        caronas = mock(CaronaRepository.class);
        service = new ReservaService(null, null, caronas, null, usuarios, reservas);
        User passageiro = new User();
        passageiro.setId(1L);
        carona = new Carona();
        carona.setId(2L);
        carona.setData(LocalDate.now());
        carona.setHora(LocalTime.MIN);
        carona.setStatusCarona(StatusCarona.EM_ESPERA);
        carona.setVagasDisponiveis(2);
        reserva = new Reserva();
        reserva.setId(3L);
        reserva.setPassageiro(passageiro);
        reserva.setCarona(carona);
        reserva.setStatusReserva(StatusReserva.ACEITA);
        when(usuarios.findByUsernameAndAtivoTrue("passageiro")).thenReturn(Optional.of(passageiro));
        when(reservas.buscarIdCarona(3L)).thenReturn(Optional.of(2L));
        when(caronas.buscarPorIdComBloqueio(2L)).thenReturn(Optional.of(carona));
        when(reservas.buscarPorIdComBloqueio(3L)).thenReturn(Optional.of(reserva));
    }

    @Test
    void permiteCancelarHojeSemCompararHorarioEDevolveVagaUmaVez() {
        assertTrue(service.cancelarReserva("passageiro", 3L).isRetorno());
        assertEquals(StatusReserva.CANCELADA, reserva.getStatusReserva());
        assertEquals(3, carona.getVagasDisponiveis());
        assertFalse(service.cancelarReserva("passageiro", 3L).isRetorno());
        assertEquals(3, carona.getVagasDisponiveis());
        verify(reservas, times(1)).save(reserva);
        verify(caronas, times(1)).save(carona);
    }

    @Test
    void permiteCancelarDataFutura() {
        carona.setData(LocalDate.now().plusDays(1));
        assertTrue(service.cancelarReserva("passageiro", 3L).isRetorno());
    }

    @Test
    void bloqueiaDiaAnterior() {
        carona.setData(LocalDate.now().minusDays(1));
        assertFalse(service.cancelarReserva("passageiro", 3L).isRetorno());
        verificarSemAlteracoes();
    }

    @Test
    void pendenteNaoDevolveVaga() {
        reserva.setStatusReserva(StatusReserva.PENDENTE);
        assertTrue(service.cancelarReserva("passageiro", 3L).isRetorno());
        assertEquals(2, carona.getVagasDisponiveis());
        verify(caronas, never()).save(any());
    }

    static Stream<StatusReserva> reservasBloqueadas() {
        return Stream.of(StatusReserva.values())
                .filter(status -> status != StatusReserva.ACEITA && status != StatusReserva.PENDENTE);
    }

    @ParameterizedTest
    @MethodSource("reservasBloqueadas")
    void bloqueiaEstadosDaReserva(StatusReserva status) {
        reserva.setStatusReserva(status);
        assertFalse(service.cancelarReserva("passageiro", 3L).isRetorno());
        verify(reservas, never()).save(any());
        verify(caronas, never()).save(any());
    }

    static Stream<StatusCarona> caronasBloqueadas() {
        return Stream.of(StatusCarona.values()).filter(status -> status != StatusCarona.EM_ESPERA);
    }

    @ParameterizedTest
    @MethodSource("caronasBloqueadas")
    void bloqueiaEstadosDaCarona(StatusCarona status) {
        carona.setStatusCarona(status);
        assertFalse(service.cancelarReserva("passageiro", 3L).isRetorno());
        verificarSemAlteracoes();
    }

    @Test
    void bloqueiaOutroPassageiro() {
        User outro = new User();
        outro.setId(4L);
        reserva.setPassageiro(outro);
        assertFalse(service.cancelarReserva("passageiro", 3L).isRetorno());
        verificarSemAlteracoes();
    }

    @Test
    void bloqueiaUsuarioAusenteOuInativo() {
        when(usuarios.findByUsernameAndAtivoTrue("passageiro")).thenReturn(Optional.empty());
        assertFalse(service.cancelarReserva("passageiro", 3L).isRetorno());
        verifyNoInteractions(reservas, caronas);
    }

    @Test
    void bloqueiaReservaInexistente() {
        when(reservas.buscarIdCarona(3L)).thenReturn(Optional.empty());
        assertFalse(service.cancelarReserva("passageiro", 3L).isRetorno());
        verifyNoInteractions(caronas);
    }

    private void verificarSemAlteracoes() {
        assertEquals(StatusReserva.ACEITA, reserva.getStatusReserva());
        assertEquals(2, carona.getVagasDisponiveis());
        verify(reservas, never()).save(any());
        verify(caronas, never()).save(any());
    }
}
