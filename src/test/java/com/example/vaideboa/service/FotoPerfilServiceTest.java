package com.example.vaideboa.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.function.Supplier;
import com.example.vaideboa.Dtos.ApiResponse;
import com.example.vaideboa.Dtos.ParticipanteChatDto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.server.ResponseStatusException;

import com.example.vaideboa.model.*;
import com.example.vaideboa.repository.*;

class FotoPerfilServiceTest {
    private final UserRepository usuarios = mock(UserRepository.class);
    private final CaronaRepository caronas = mock(CaronaRepository.class);
    private final PedidoCaronaRepository pedidos = mock(PedidoCaronaRepository.class);
    private final ReservaRepository reservas = mock(ReservaRepository.class);
    private final ChatRepository chats = mock(ChatRepository.class);
    private final AvaliacaoRepository avaliacoes = mock(AvaliacaoRepository.class);
    private final FotoStorageService storage = mock(FotoStorageService.class);
    private final FotoPerfilService service = new FotoPerfilService(
            usuarios, caronas, pedidos, reservas, chats, avaliacoes, storage);
    private User motorista;
    private User passageiro;
    private User terceiro;
    private Carona carona;
    private Reserva reserva;
    private Avaliacao avaliacao;

    @BeforeEach
    void configurar() {
        motorista = usuario(1L, "motorista");
        passageiro = usuario(2L, "passageiro");
        terceiro = usuario(3L, "terceiro");
        carona = new Carona();
        carona.setId(10L);
        carona.setMotorista(motorista);
        reserva = new Reserva();
        reserva.setId(20L);
        reserva.setCarona(carona);
        reserva.setPassageiro(passageiro);
        avaliacao = new Avaliacao();
        avaliacao.setAvaliador(motorista);
        avaliacao.setAvaliado(passageiro);
        when(caronas.findById(10L)).thenReturn(Optional.of(carona));
        when(reservas.findById(20L)).thenReturn(Optional.of(reserva));
        when(chats.findByReserva(reserva)).thenReturn(Optional.of(new Chat()));
        when(avaliacoes.findById(30L)).thenReturn(Optional.of(avaliacao));
        when(storage.carregar(anyString())).thenReturn(
                new FotoStorageService.Foto(new byte[]{1, 2}, MediaType.IMAGE_PNG));
    }

    private User usuario(Long id, String nome) {
        User usuario = new User();
        usuario.setId(id);
        usuario.setNome(nome);
        usuario.setFoto("referencia-" + id);
        when(usuarios.findById(id)).thenReturn(Optional.of(usuario));
        when(usuarios.findByUsernameAndAtivoTrue(nome)).thenReturn(Optional.of(usuario));
        return usuario;
    }

    private void negar(Supplier<ApiResponse> acao, String mensagem) {
        ApiResponse retorno = acao.get();
        assertFalse(retorno.isRetorno());
        assertEquals(mensagem, retorno.getMensagem());
        assertNull(retorno.getDados());
        verifyNoInteractions(storage);
    }

    @Test
    void permiteFotoDoMotoristaNaBuscaSemPedido() {
        ApiResponse retorno = service.buscarFotoPerfil("terceiro", 1L, 10L);
        assertTrue(retorno.isRetorno());
        assertInstanceOf(FotoStorageService.Foto.class, retorno.getDados());
        verify(storage).carregar(motorista.getFoto());
    }

    @Test
    void motoristaPodeVerPassageiroComPedidoInclusivePendente() {
        when(pedidos.existsByPassageiroAndCarona(passageiro, carona)).thenReturn(true);
        service.buscarFotoPerfil("motorista", 2L, 10L);
        verify(storage).carregar(passageiro.getFoto());
    }

    @Test
    void motoristaNaoPodeVerPessoaSemVinculoComCarona() {
        negar(() -> service.buscarFotoPerfil("motorista", 3L, 10L), "Usuário não tem acesso a este perfil");
    }

    @Test
    void terceiroNaoPodeConsultarFotoDePassageiro() {
        when(pedidos.existsByPassageiroAndCarona(passageiro, carona)).thenReturn(true);
        negar(() -> service.buscarFotoPerfil("terceiro", 2L, 10L), "Usuário não tem acesso a este perfil");
    }

    @Test
    void naoAceitaMotoristaDeOutraCaronaComoAlvo() {
        negar(() -> service.buscarFotoPerfil("passageiro", 3L, 10L), "Usuário não tem acesso a este perfil");
    }

    @Test
    void naoConsultaPerfilInativo() {
        passageiro.setAtivo(false);
        negar(() -> service.buscarFotoPerfil("motorista", 2L, 10L), "Perfil não encontrado");
    }

    @Test
    void rejeitaIdentificadorInvalidoEUsuarioInativo() {
        negar(() -> service.buscarFotoPerfil("motorista", 2L, 0L), "Informe um identificador positivo");
        negar(() -> service.buscarFotoPerfil("inativo", 2L, 10L), "Usuário não encontrado");
    }

    @Test
    void chatSelecionaOutroParticipanteNosDoisSentidos() {
        assertEquals(2L, ((ParticipanteChatDto) service.buscarParticipanteChat("motorista", 20L).getDados()).idUsuario());
        assertEquals(com.example.vaideboa.Dtos.ParticipanteChatDto.Papel.PASSAGEIRO,
                ((ParticipanteChatDto) service.buscarParticipanteChat("motorista", 20L).getDados()).papel());
        assertEquals(com.example.vaideboa.Dtos.ParticipanteChatDto.Papel.MOTORISTA,
                ((ParticipanteChatDto) service.buscarParticipanteChat("passageiro", 20L).getDados()).papel());
        assertEquals(1L, ((ParticipanteChatDto) service.buscarParticipanteChat("passageiro", 20L).getDados()).idUsuario());
        assertEquals("/fotos/chat/20", ((ParticipanteChatDto) service.buscarParticipanteChat("motorista", 20L).getDados()).fotoUrl());
        service.buscarFotoChat("motorista", 20L);
        service.buscarFotoChat("passageiro", 20L);
        verify(storage).carregar(passageiro.getFoto());
        verify(storage).carregar(motorista.getFoto());
    }

    @Test
    void terceiroNaoAcessaMetadadosNemFotoDoChat() {
        negar(() -> service.buscarParticipanteChat("terceiro", 20L), "Usuário não faz parte dessa reserva");
        negar(() -> service.buscarFotoChat("terceiro", 20L), "Usuário não faz parte dessa reserva");
    }

    @Test
    void reservaSemChatNaoLiberaFoto() {
        when(chats.findByReserva(reserva)).thenReturn(Optional.empty());
        negar(() -> service.buscarFotoChat("motorista", 20L), "Chat não encontrado");
    }

    @Test
    void chatSemFotoRetornaMetadadosComUrlNula() {
        passageiro.setFoto(null);
        assertNull(((ParticipanteChatDto) service.buscarParticipanteChat("motorista", 20L).getDados()).fotoUrl());
        verifyNoInteractions(storage);
    }

    @Test
    void avaliadorRecebeFotoDoAvaliado() {
        service.buscarFotoAvaliado("motorista", 30L);
        verify(storage).carregar(passageiro.getFoto());
    }

    @Test
    void avaliadoETerceiroNaoPodemUsarPermissaoDoAvaliador() {
        negar(() -> service.buscarFotoAvaliado("passageiro", 30L), "Usuário não tem acesso a esta avaliação");
        negar(() -> service.buscarFotoAvaliado("terceiro", 30L), "Usuário não tem acesso a esta avaliação");
    }

    @Test
    void recursoInexistenteRetornaFalha() {
        negar(() -> service.buscarFotoPerfil("motorista", 2L, 99L), "Carona não encontrada");
        negar(() -> service.buscarFotoChat("motorista", 99L), "Reserva não encontrada");
        negar(() -> service.buscarFotoAvaliado("motorista", 99L), "Avaliação não encontrada");
    }

    @Test
    void retornaFalhaQuandoArquivoNaoExiste() {
        when(storage.carregar(motorista.getFoto())).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Foto não encontrada"));
        ApiResponse retorno = service.buscarFotoPerfil("terceiro", 1L, 10L);
        assertFalse(retorno.isRetorno());
        assertEquals("Foto não encontrada", retorno.getMensagem());
    }

    @Test
    void falhaDeLeituraDoServidorContinuaErroInterno() {
        when(storage.carregar(motorista.getFoto())).thenThrow(
                new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Não foi possível ler a foto"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, assertThrows(ResponseStatusException.class,
                () -> service.buscarFotoPerfil("terceiro", 1L, 10L)).getStatusCode());
    }

    @Test
    void fotoAusenteRetornaFalhaSemAcessarDisco() {
        motorista.setFoto(null);
        negar(() -> service.buscarFotoPerfil("terceiro", 1L, 10L), "Usuário sem foto");
    }

    // @Test
    // void urlsNaoExpoemReferenciaInternaESaoNulasSemFoto() {
    //     assertEquals("/fotos/perfil/2?idCarona=10", FotoPerfilService.urlPerfil(passageiro, 10L));
    //     assertEquals("/fotos/avaliacao/30", FotoPerfilService.urlAvaliado(passageiro, 30L));
    //     passageiro.setFoto(" ");
    //     assertNull(FotoPerfilService.urlPerfil(passageiro, 10L));
    //     passageiro.setAtivo(false);
    //     assertNull(FotoPerfilService.urlAvaliado(passageiro, 30L));
    // }
}
