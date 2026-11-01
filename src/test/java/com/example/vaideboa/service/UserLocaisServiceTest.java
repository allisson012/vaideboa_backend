package com.example.vaideboa.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;

import com.example.vaideboa.Dtos.ApiResponse;
import com.example.vaideboa.Dtos.LocaisUsuarioDto;
import com.example.vaideboa.model.Casa;
import com.example.vaideboa.model.Faculdade;
import com.example.vaideboa.model.User;
import com.example.vaideboa.repository.UserRepository;

class UserLocaisServiceTest {
    private final UserRepository usuarios = mock(UserRepository.class);
    private final UserService service = new UserService(null, usuarios, null, null, null, null, null, null);
    private final GeometryFactory geometryFactory = new GeometryFactory();
    private User user;

    @BeforeEach
    void configurar() {
        user = new User();
        user.setMinhasCasas(List.of());
        user.setMinhasFaculdades(List.of());
        when(usuarios.findByUsernameAndAtivoTrue("usuario")).thenReturn(Optional.of(user));
    }

    @Test
    void retornaCasaEFaculdadeComCoordenadasCorretas() {
        user.setMinhasCasas(List.of(casa(1L)));
        user.setMinhasFaculdades(List.of(faculdade()));

        LocaisUsuarioDto locais = consultar();

        assertEquals(1L, locais.getCasa().getId());
        assertEquals("Minha casa", locais.getCasa().getDescricao());
        assertEquals(-22.537, locais.getCasa().getLatitude());
        assertEquals(-44.774, locais.getCasa().getLongitude());
        assertEquals(3L, locais.getFaculdade().getId());
        assertEquals("Minha faculdade", locais.getFaculdade().getDescricao());
        assertEquals(-22.508, locais.getFaculdade().getLatitude());
        assertEquals(-44.908, locais.getFaculdade().getLongitude());
        verify(usuarios).findByUsernameAndAtivoTrue("usuario");
        verifyNoMoreInteractions(usuarios);
    }

    @Test
    void retornaSomenteCasaQuandoNaoHaFaculdade() {
        user.setMinhasCasas(List.of(casa(1L)));
        LocaisUsuarioDto locais = consultar();
        assertNotNull(locais.getCasa());
        assertNull(locais.getFaculdade());
    }

    @Test
    void retornaSomenteFaculdadeQuandoNaoHaCasa() {
        user.setMinhasFaculdades(List.of(faculdade()));
        LocaisUsuarioDto locais = consultar();
        assertNull(locais.getCasa());
        assertNotNull(locais.getFaculdade());
    }

    @Test
    void retornaCamposNulosQuandoNaoHaLocais() {
        LocaisUsuarioDto locais = consultar();
        assertNull(locais.getCasa());
        assertNull(locais.getFaculdade());
    }

    @Test
    void retornaCamposNulosQuandoRelacionamentosSaoNulos() {
        user.setMinhasCasas(null);
        user.setMinhasFaculdades(null);
        LocaisUsuarioDto locais = consultar();
        assertNull(locais.getCasa());
        assertNull(locais.getFaculdade());
    }

    @Test
    void selecionaMenorIdQuandoHaDuplicadosAntigos() {
        user.setMinhasCasas(List.of(casa(2L), casa(1L)));
        Faculdade maisNova = faculdade();
        maisNova.setId(4L);
        user.setMinhasFaculdades(List.of(maisNova, faculdade()));
        LocaisUsuarioDto locais = consultar();
        assertEquals(1L, locais.getCasa().getId());
        assertEquals(3L, locais.getFaculdade().getId());
    }

    @Test
    void rejeitaUsuarioAusenteOuInativo() {
        ApiResponse resposta = service.buscarLocais("inexistente");
        assertFalse(resposta.isRetorno());
        assertNull(resposta.getDados());
    }

    private LocaisUsuarioDto consultar() {
        ApiResponse resposta = service.buscarLocais("usuario");
        assertTrue(resposta.isRetorno());
        return assertInstanceOf(LocaisUsuarioDto.class, resposta.getDados());
    }

    private Casa casa(Long id) {
        Casa casa = new Casa();
        casa.setId(id);
        casa.setDescricao("Minha casa");
        casa.setLocalizacao(geometryFactory.createPoint(new Coordinate(-44.774, -22.537)));
        casa.setUser(user);
        return casa;
    }

    private Faculdade faculdade() {
        Faculdade faculdade = new Faculdade();
        faculdade.setId(3L);
        faculdade.setDescrisao("Minha faculdade");
        faculdade.setLocalizacao(geometryFactory.createPoint(new Coordinate(-44.908, -22.508)));
        faculdade.setUser(user);
        return faculdade;
    }
}
