package com.example.vaideboa.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import com.example.vaideboa.Dtos.FaculdadeDto;
import com.example.vaideboa.model.Faculdade;
import com.example.vaideboa.model.User;
import com.example.vaideboa.repository.FaculdadeRepository;
import com.example.vaideboa.repository.UserRepository;

class FaculdadeServiceTest {
    private final UserRepository usuarios = mock(UserRepository.class);
    private final FaculdadeRepository faculdades = mock(FaculdadeRepository.class);
    private final FaculdadeService service = new FaculdadeService(usuarios, faculdades);
    private User user;
    private FaculdadeDto dto;

    @BeforeEach
    void configurar() {
        user = new User();
        user.setId(10L);
        dto = new FaculdadeDto("Novo endereço", -22.537, -44.774);
        when(usuarios.buscarAtivoPorUsernameComBloqueio("usuario")).thenReturn(Optional.of(user));
    }

    @Test
    void criaQuandoAindaNaoHaCadastro() {
        assertTrue(service.cadastrarFaculdade("usuario", dto).isRetorno());

        ArgumentCaptor<Faculdade> captor = ArgumentCaptor.forClass(Faculdade.class);
        InOrder ordem = inOrder(usuarios, faculdades);
        ordem.verify(usuarios).buscarAtivoPorUsernameComBloqueio("usuario");
        ordem.verify(faculdades).findFirstByUserOrderByIdAsc(user);
        ordem.verify(faculdades).save(captor.capture());
        Faculdade salvo = captor.getValue();
        assertNull(salvo.getId());
        assertSame(user, salvo.getUser());
        assertEquals("Novo endereço", salvo.getDescrisao());
        assertEquals(-22.537, salvo.getLocalizacao().getY());
        assertEquals(-44.774, salvo.getLocalizacao().getX());
        assertEquals(4326, salvo.getLocalizacao().getSRID());
    }

    @Test
    void atualizaMesmoRegistroEmSalvamentosRepetidos() {
        Faculdade existente = new Faculdade();
        existente.setId(20L);
        existente.setUser(user);
        existente.setDescrisao("Endereço antigo");
        when(faculdades.findFirstByUserOrderByIdAsc(user)).thenReturn(Optional.of(existente));

        assertTrue(service.cadastrarFaculdade("usuario", dto).isRetorno());
        assertEquals("Novo endereço", existente.getDescrisao());
        assertEquals(-22.537, existente.getLocalizacao().getY());
        assertEquals(-44.774, existente.getLocalizacao().getX());
        assertEquals(20L, existente.getId());

        dto = new FaculdadeDto("Outro endereço", -23.5, -46.6);
        assertTrue(service.cadastrarFaculdade("usuario", dto).isRetorno());
        assertEquals("Outro endereço", existente.getDescrisao());
        assertEquals(-23.5, existente.getLocalizacao().getY());
        assertEquals(-46.6, existente.getLocalizacao().getX());
        assertEquals(20L, existente.getId());
        verify(faculdades, times(2)).save(same(existente));
    }

    @Test
    void rejeitaUsuarioAusenteOuInativoSemSalvar() {
        assertFalse(service.cadastrarFaculdade("inexistente", dto).isRetorno());
        verifyNoInteractions(faculdades);
    }
}
