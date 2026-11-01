package com.example.vaideboa.controller;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.vaideboa.Dtos.ParticipanteChatDto;
import com.example.vaideboa.Dtos.ApiResponse;
import com.example.vaideboa.Dtos.ParticipanteChatDto.Papel;
import com.example.vaideboa.security.*;
import com.example.vaideboa.service.FotoPerfilService;
import com.example.vaideboa.service.FotoStorageService.Foto;

@WebMvcTest(properties = {
    "spring.security.oauth2.client.registration.google.client-id=teste",
    "spring.security.oauth2.client.registration.google.client-secret=teste",
    "spring.security.oauth2.client.registration.github.client-id=teste",
    "spring.security.oauth2.client.registration.github.client-secret=teste"
})
@ContextConfiguration(classes = FotoPerfilController.class)
@Import(SecurityConfig.class)
class FotoPerfilHttpTest {
    @Autowired
    private MockMvc mvc;
    @MockitoBean
    private FotoPerfilService service;
    @MockitoBean
    private CustomOAuth2UserService oauthUsers;
    @MockitoBean
    private OAuth2SuccessHandler oauthHandler;
    @MockitoBean
    private JwtDecoder jwtDecoder;
    @MockitoBean
    private JwtEncoder jwtEncoder;

    @ParameterizedTest
    @ValueSource(strings = {"/fotos/perfil/2?idCarona=10", "/fotos/chat/20",
            "/fotos/chat/20/participante", "/fotos/avaliacao/30"})
    void exigeAutenticacao(String rota) throws Exception {
        mvc.perform(get(rota).accept(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized());
        verifyNoInteractions(service);
    }

    @ParameterizedTest
    @ValueSource(strings = {"image/png", "image/jpeg"})
    void retornaBytesComTipoCorretoSemCache(String tipo) throws Exception {
        Foto foto = new Foto(new byte[]{1, 2, 3}, MediaType.parseMediaType(tipo));
        when(service.buscarFotoPerfil("motorista", 2L, 10L)).thenReturn(new ApiResponse(true, "Foto encontrada", foto));
        when(service.buscarFotoChat("motorista", 20L)).thenReturn(new ApiResponse(true, "Foto encontrada", foto));
        when(service.buscarFotoAvaliado("motorista", 30L)).thenReturn(new ApiResponse(true, "Foto encontrada", foto));
        for (String rota : new String[]{"/fotos/perfil/2?idCarona=10", "/fotos/chat/20", "/fotos/avaliacao/30"}) {
            mvc.perform(get(rota).with(jwt().jwt(token -> token.subject("motorista"))))
                    .andExpect(status().isOk()).andExpect(content().bytes(foto.bytes()))
                    .andExpect(content().contentType(tipo))
                    .andExpect(header().string("Cache-Control", "no-store"))
                    .andExpect(header().string("X-Content-Type-Options", "nosniff"));
        }
    }

    @Test
    void identificaParticipanteEPapelSemExporReferenciaInterna() throws Exception {
        when(service.buscarParticipanteChat("motorista", 20L)).thenReturn(
                new ApiResponse(true, "Participante encontrado",
                        new ParticipanteChatDto(2L, "Maria", Papel.PASSAGEIRO, "/fotos/chat/20")));
        mvc.perform(get("/fotos/chat/20/participante").with(jwt().jwt(token -> token.subject("motorista"))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.idUsuario").value(2))
                .andExpect(jsonPath("$.nome").value("Maria"))
                .andExpect(jsonPath("$.papel").value("PASSAGEIRO"))
                .andExpect(jsonPath("$.fotoUrl").value("/fotos/chat/20"))
                .andExpect(jsonPath("$.foto").doesNotExist())
                .andExpect(header().string("Cache-Control", "no-store"));
    }

    @Test
    void participanteSemFotoMantemIdentificacao() throws Exception {
        when(service.buscarParticipanteChat("passageiro", 20L)).thenReturn(
                new ApiResponse(true, "Participante encontrado",
                        new ParticipanteChatDto(1L, "João", Papel.MOTORISTA, null)));
        mvc.perform(get("/fotos/chat/20/participante").with(jwt().jwt(token -> token.subject("passageiro"))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.papel").value("MOTORISTA"))
                .andExpect(jsonPath("$.idUsuario").value(1)).andExpect(jsonPath("$.fotoUrl").isEmpty());
    }

    @Test
    void distingueFotoAusenteDeAcessoNegado() throws Exception {
        when(service.buscarFotoChat("motorista", 20L)).thenReturn(new ApiResponse(false, "Foto não encontrada"));
        when(service.buscarFotoChat("terceiro", 20L)).thenReturn(new ApiResponse(false, "Usuário não faz parte dessa reserva"));
        mvc.perform(get("/fotos/chat/20").with(jwt().jwt(token -> token.subject("motorista"))))
                .andExpect(status().isBadRequest()).andExpect(content().string("Foto não encontrada"));
        mvc.perform(get("/fotos/chat/20").with(jwt().jwt(token -> token.subject("terceiro"))))
                .andExpect(status().isBadRequest()).andExpect(content().string("Usuário não faz parte dessa reserva"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"/fotos/perfil/2?idCarona=10", "/fotos/chat/20",
            "/fotos/chat/20/participante", "/fotos/avaliacao/30"})
    void falhaRetornaBadRequestComMensagem(String rota) throws Exception {
        ApiResponse retorno = new ApiResponse(false, "Usuário não encontrado");
        when(service.buscarFotoPerfil("inativo", 2L, 10L)).thenReturn(retorno);
        when(service.buscarFotoChat("inativo", 20L)).thenReturn(retorno);
        when(service.buscarParticipanteChat("inativo", 20L)).thenReturn(retorno);
        when(service.buscarFotoAvaliado("inativo", 30L)).thenReturn(retorno);
        mvc.perform(get(rota).with(jwt().jwt(token -> token.subject("inativo"))))
                .andExpect(status().isBadRequest()).andExpect(content().string("Usuário não encontrado"));
    }

    @Test
    void exigeContextoDaCarona() throws Exception {
        mvc.perform(get("/fotos/perfil/2").with(jwt())).andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }
}
