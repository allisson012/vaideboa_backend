package com.example.vaideboa.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import com.example.vaideboa.Dtos.CasaDto;
import com.example.vaideboa.model.Casa;
import com.example.vaideboa.model.User;
import com.example.vaideboa.repository.CasaRepository;
import com.example.vaideboa.repository.UserRepository;

class CasaServiceTest {
    private final UserRepository usuarios = mock(UserRepository.class);
    private final CasaRepository casas = mock(CasaRepository.class);
    private final CasaService service = new CasaService(usuarios, casas);
    private User user;
    private CasaDto dto;

    @BeforeEach
    void configurar() {
        user = new User();
        user.setId(10L);
        dto = new CasaDto("Novo endereço", -22.537, -44.774);
        when(usuarios.buscarAtivoPorUsernameComBloqueio("usuario")).thenReturn(Optional.of(user));
    }

    @Test
    void criaQuandoAindaNaoHaCadastro() {
        assertTrue(service.cadastrarCasa("usuario", dto).isRetorno());

        ArgumentCaptor<Casa> captor = ArgumentCaptor.forClass(Casa.class);
        InOrder ordem = inOrder(usuarios, casas);
        ordem.verify(usuarios).buscarAtivoPorUsernameComBloqueio("usuario");
        ordem.verify(casas).findFirstByUserOrderByIdAsc(user);
        ordem.verify(casas).save(captor.capture());
        Casa salvo = captor.getValue();
        assertNull(salvo.getId());
        assertSame(user, salvo.getUser());
        assertEquals("Novo endereço", salvo.getDescricao());
        assertEquals(-22.537, salvo.getLocalizacao().getY());
        assertEquals(-44.774, salvo.getLocalizacao().getX());
        assertEquals(4326, salvo.getLocalizacao().getSRID());
    }

    @Test
    void atualizaMesmoRegistroEmSalvamentosRepetidos() {
        Casa existente = new Casa();
        existente.setId(20L);
        existente.setUser(user);
        existente.setDescricao("Endereço antigo");
        when(casas.findFirstByUserOrderByIdAsc(user)).thenReturn(Optional.of(existente));

        assertTrue(service.cadastrarCasa("usuario", dto).isRetorno());
        assertEquals("Novo endereço", existente.getDescricao());
        assertEquals(-22.537, existente.getLocalizacao().getY());
        assertEquals(-44.774, existente.getLocalizacao().getX());
        assertEquals(20L, existente.getId());

        dto = new CasaDto("Outro endereço", -23.5, -46.6);
        assertTrue(service.cadastrarCasa("usuario", dto).isRetorno());
        assertEquals("Outro endereço", existente.getDescricao());
        assertEquals(-23.5, existente.getLocalizacao().getY());
        assertEquals(-46.6, existente.getLocalizacao().getX());
        assertEquals(20L, existente.getId());
        verify(casas, times(2)).save(same(existente));
    }

    @Test
    void rejeitaUsuarioAusenteOuInativoSemSalvar() {
        assertFalse(service.cadastrarCasa("inexistente", dto).isRetorno());
        verifyNoInteractions(casas);
    }
}
