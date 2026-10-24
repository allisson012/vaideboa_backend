package com.example.vaideboa.controller;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;
import com.example.vaideboa.config.WebConfig;
import com.example.vaideboa.Dtos.*;
import com.example.vaideboa.security.*;
import com.example.vaideboa.service.CompartilhamentoExternoService;

@WebMvcTest(properties = {
    "spring.security.oauth2.client.registration.google.client-id=teste",
    "spring.security.oauth2.client.registration.google.client-secret=teste",
    "spring.security.oauth2.client.registration.github.client-id=teste",
    "spring.security.oauth2.client.registration.github.client-secret=teste"
})
@ContextConfiguration(classes = {CompartilhamentoPublicoController.class, CompartilhamentoCaronaController.class})
@Import({SecurityConfig.class, WebConfig.class})
class CompartilhamentoHttpTest {
    @Autowired
    private MockMvc mvc;
    @MockitoBean
    private CompartilhamentoExternoService service;
    @MockitoBean
    private CustomOAuth2UserService oauthUsers;
    @MockitoBean
    private OAuth2SuccessHandler oauthHandler;
    @MockitoBean
    private JwtDecoder jwtDecoder;
    @MockitoBean
    private JwtEncoder jwtEncoder;

    private final String token = "a".repeat(43);

    @Test
    void consultaPublicaSemJwtRetornaSomenteContratoPublico() throws Exception {
        when(service.consultar(token)).thenReturn(new AcompanhamentoPublicoDto("EM_ANDAMENTO", "DISPONIVEL",
                new AcompanhamentoPublicoDto.Coordenadas(-23, -46), Instant.parse("2026-10-24T15:00:00Z")));
        mvc.perform(get("/compartilhamento/publico/" + token))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(header().string("Referrer-Policy", "no-referrer"))
                .andExpect(jsonPath("$.localizacao.latitude").value(-23))
                .andExpect(jsonPath("$.localizacao.longitude").value(-46))
                .andExpect(jsonPath("$.id").doesNotExist())
                .andExpect(jsonPath("$.token").doesNotExist())
                .andExpect(jsonPath("$.carona").doesNotExist())
                .andExpect(jsonPath("$.localizacao.idCarona").doesNotExist());
    }

    @Test
    void erroPublicoTemRespostaGenericaSemCache() throws Exception {
        when(service.consultar(token)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));
        mvc.perform(get("/compartilhamento/publico/" + token))
                .andExpect(status().isNotFound())
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(jsonPath("$.codigo").value("ACOMPANHAMENTO_INDISPONIVEL"))
                .andExpect(jsonPath("$.localizacao").doesNotExist());
    }

    @Test
    void falhaTemporariaNaoExpoeErroOuLocalizacao() throws Exception {
        when(service.consultar(token)).thenThrow(new IllegalStateException("detalhe interno"));
        mvc.perform(get("/compartilhamento/publico/" + token))
                .andExpect(status().isServiceUnavailable())
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(jsonPath("$.codigo").value("SERVICO_INDISPONIVEL"))
                .andExpect(jsonPath("$.localizacao").doesNotExist());
    }

    @Test
    void obterLinkExigeJwt() throws Exception {
        mvc.perform(post("/carona/3/compartilhamento").header("Accept", "application/json"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(service);
    }

    @Test
    void tokenNaUrlNaoLiberaEndpointPrivado() throws Exception {
        mvc.perform(get("/carona/buscar/3").param("token", token).header("Accept", "application/json"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void liberarConsultaNaoLiberaPostPublico() throws Exception {
        mvc.perform(post("/compartilhamento/publico/" + token).header("Accept", "application/json"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(service);
    }

    @Test
    void usuarioAutenticadoObtemLinkSemCache() throws Exception {
        when(service.obterOuCriarLink(3L, "motorista")).thenReturn(new CompartilhamentoCaronaDto(
                "https://exemplo.com/v#" + token, Instant.parse("2026-10-25T15:00:00Z")));
        mvc.perform(post("/carona/3/compartilhamento").with(jwt().jwt(jwt -> jwt.subject("motorista"))))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(jsonPath("$.url").value("https://exemplo.com/v#" + token));
    }

    @Test
    void erroPrivadoTambemNaoPodeSerArmazenadoEmCache() throws Exception {
        when(service.obterOuCriarLink(3L, "passageiro")).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));
        mvc.perform(post("/carona/3/compartilhamento").with(jwt().jwt(jwt -> jwt.subject("passageiro"))))
                .andExpect(status().isNotFound())
                .andExpect(header().string("Cache-Control", "no-store"));
    }

    @Test
    void origemPermitidaConsultaSemCredenciais() throws Exception {
        when(service.consultar(token)).thenReturn(new AcompanhamentoPublicoDto(
                "EM_ANDAMENTO", "AGUARDANDO_PRIMEIRA_LOCALIZACAO", null, null));
        mvc.perform(get("/compartilhamento/publico/" + token).header("Origin", "http://localhost:5173"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"))
                .andExpect(header().doesNotExist("Access-Control-Allow-Credentials"));
    }

    @Test
    void origemDesconhecidaEhBloqueada() throws Exception {
        mvc.perform(get("/compartilhamento/publico/" + token).header("Origin", "https://desconhecido.com"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(service);
    }
}
