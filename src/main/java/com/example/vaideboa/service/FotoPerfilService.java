package com.example.vaideboa.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.example.vaideboa.Dtos.ApiResponse;
import com.example.vaideboa.Dtos.ParticipanteChatDto;
import com.example.vaideboa.Dtos.ParticipanteChatDto.Papel;
import com.example.vaideboa.model.Avaliacao;
import com.example.vaideboa.model.Carona;
import com.example.vaideboa.model.Chat;
import com.example.vaideboa.model.Reserva;
import com.example.vaideboa.model.User;
import com.example.vaideboa.repository.AvaliacaoRepository;
import com.example.vaideboa.repository.CaronaRepository;
import com.example.vaideboa.repository.ChatRepository;
import com.example.vaideboa.repository.PedidoCaronaRepository;
import com.example.vaideboa.repository.ReservaRepository;
import com.example.vaideboa.repository.UserRepository;

@Service
@Transactional(readOnly = true)
public class FotoPerfilService {
    private final UserRepository userRepository;
    private final CaronaRepository caronaRepository;
    private final PedidoCaronaRepository pedidoCaronaRepository;
    private final ReservaRepository reservaRepository;
    private final ChatRepository chatRepository;
    private final AvaliacaoRepository avaliacaoRepository;
    private final FotoStorageService fotoStorageService;

    

    public FotoPerfilService(UserRepository userRepository, CaronaRepository caronaRepository,
            PedidoCaronaRepository pedidoCaronaRepository, ReservaRepository reservaRepository,
            ChatRepository chatRepository, AvaliacaoRepository avaliacaoRepository,
            FotoStorageService fotoStorageService) {
        this.userRepository = userRepository;
        this.caronaRepository = caronaRepository;
        this.pedidoCaronaRepository = pedidoCaronaRepository;
        this.reservaRepository = reservaRepository;
        this.chatRepository = chatRepository;
        this.avaliacaoRepository = avaliacaoRepository;
        this.fotoStorageService = fotoStorageService;
    }

    @Transactional
    public ApiResponse cadastrarFotoPerfil(String username, MultipartFile arquivo) {
        Optional<User> userOpt = userRepository.findByUsernameAndAtivoTrue(username);
        if (userOpt.isEmpty()) {
            return new ApiResponse(false, "Usuário não encontrado");
        }
        User user = userOpt.get();
        String fotoAnterior = user.getFoto();
        String referencia = fotoStorageService.salvar(user.getId(), arquivo);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status == STATUS_COMMITTED) {
                    fotoStorageService.removerSeExistir(fotoAnterior);
                } else if (status == STATUS_ROLLED_BACK) {
                    fotoStorageService.removerSeExistir(referencia);
                }
            }
        });

        user.setFoto(referencia);
        userRepository.save(user);
        return new ApiResponse(true, "Foto cadastrada com sucesso");
    }

    public ApiResponse buscarMinhaFoto(String username) {
        Optional<User> userOpt = userRepository.findByUsernameAndAtivoTrue(username);
        if (userOpt.isEmpty()) {
            return new ApiResponse(false, "Usuário não encontrado");
        }
        User user = userOpt.get();
        return carregarFoto(user);
    }

    public ApiResponse buscarPerfilAutorizado(String username, Long idUsuario, Long idCarona) {
        if (idUsuario == null || idUsuario <= 0 || idCarona == null || idCarona <= 0 ) {
            return new ApiResponse(false, "Informe um identificador positivo");
        }
        Optional<User> userOpt = userRepository.findByUsernameAndAtivoTrue(username);
        if (userOpt.isEmpty()) {
            return new ApiResponse(false, "Usuário não encontrado");
        }
        User solicitante = userOpt.get();
        Optional<User> perfilOpt = userRepository.findById(idUsuario);
        if (perfilOpt.isEmpty() || !perfilOpt.get().isAtivo()) {
            return new ApiResponse(false, "Perfil não encontrado");
        }
        User perfil = perfilOpt.get();
        Optional<Carona> caronaOpt = caronaRepository.findById(idCarona);
        if (caronaOpt.isEmpty()) {
            return new ApiResponse(false, "Carona não encontrada");
        }
        Carona carona = caronaOpt.get();
        boolean motorista = carona.getMotorista().getId().equals(perfil.getId());
        boolean proprioPerfil = solicitante.getId().equals(perfil.getId());
        boolean donoDaCarona = carona.getMotorista().getId().equals(solicitante.getId());
        if (!motorista && !proprioPerfil && !(donoDaCarona && pedidoCaronaRepository.existsByPassageiroAndCarona(perfil, carona))){
            return new ApiResponse(false, "Usuário não tem acesso a este perfil");
        }
        return new ApiResponse(true, "Perfil encontrado", perfil);
    }

    public ApiResponse buscarFotoPerfil(String username, Long idUsuario, Long idCarona) {
        ApiResponse retorno = buscarPerfilAutorizado(username, idUsuario, idCarona);
        if (!retorno.isRetorno()) {
            return retorno;
        }
        User perfil = (User) retorno.getDados();
        return carregarFoto(perfil);
    }

    public ApiResponse buscarParticipanteChat(String username, Long idReserva) {
        ApiResponse retorno = outroParticipante(username, idReserva);
        if (!retorno.isRetorno()) {
            return retorno;
        }
        Participante participante = (Participante) retorno.getDados();
        User outro = participante.usuario();
        String fotoUrl;
        if (temFoto(outro)) {
            fotoUrl = "/fotos/chat/" + idReserva;
        } else {
            fotoUrl = null;
        }
        ParticipanteChatDto dto = new ParticipanteChatDto(outro.getId(),outro.getNome(),participante.papel(),fotoUrl);
        return new ApiResponse(true, "Participante encontrado", dto);
    }

    public ApiResponse buscarFotoChat(String username, Long idReserva) {
        ApiResponse retorno = outroParticipante(username, idReserva);
        if (!retorno.isRetorno()) {
            return retorno;
        }
        Participante participante = (Participante) retorno.getDados();
        return carregarFoto(participante.usuario());
    }

    private ApiResponse outroParticipante(String username, Long idReserva) {
        if (idReserva == null || idReserva <= 0) {
            return new ApiResponse(false, "Informe um identificador positivo");
        }
        Optional<User> userOpt = userRepository.findByUsernameAndAtivoTrue(username);
        if (userOpt.isEmpty()) {
            return new ApiResponse(false, "Usuário não encontrado");
        }
        User solicitante = userOpt.get();
        Optional<Reserva> reservaOpt = reservaRepository.findById(idReserva);
        if (reservaOpt.isEmpty()) {
            return new ApiResponse(false, "Reserva não encontrada");
        }
        Reserva reserva = reservaOpt.get();
        User motorista = reserva.getCarona().getMotorista();
        User passageiro = reserva.getPassageiro();
        boolean ehMotorista = motorista.getId().equals(solicitante.getId());
        if (!ehMotorista && !passageiro.getId().equals(solicitante.getId())) {
            return new ApiResponse(false, "Usuário não faz parte dessa reserva");
        }
        Optional<Chat> chatOpt = chatRepository.findByReserva(reserva);
        if (chatOpt.isEmpty()) {
            return new ApiResponse(false, "Chat não encontrado");
        }
        User outro;
        Papel papel;
        if(ehMotorista){
            outro = passageiro;
            papel = Papel.PASSAGEIRO;
        }else{
            outro = motorista;
            papel = Papel.MOTORISTA;
        }
        if (!outro.isAtivo()) {
            return new ApiResponse(false, "Usuário não encontrado ou inativo");
        }
        Participante participante = new Participante(outro, papel);
        return new ApiResponse(true, "Participante encontrado",participante);
    }

    public ApiResponse buscarFotoAvaliado(String username, Long idAvaliacao) {
        if (idAvaliacao == null || idAvaliacao <= 0) {
            return new ApiResponse(false, "Informe um identificador positivo");
        }
        Optional<User> userOpt = userRepository.findByUsernameAndAtivoTrue(username);
        if (userOpt.isEmpty()) {
            return new ApiResponse(false, "Usuário não encontrado");
        }
        User solicitante = userOpt.get();
        Optional<Avaliacao> avaliacaoOpt = avaliacaoRepository.findById(idAvaliacao);
        if (avaliacaoOpt.isEmpty()) {
            return new ApiResponse(false, "Avaliação não encontrada");
        }
        Avaliacao avaliacao = avaliacaoOpt.get();
        if (!avaliacao.getAvaliador().getId().equals(solicitante.getId())) {
            return new ApiResponse(false, "Usuário não tem acesso a esta avaliação");
        }
        User avaliado = avaliacao.getAvaliado();
        if (!avaliado.isAtivo()) {
            return new ApiResponse(false, "Usuário não encontrado ou inativo");
        }
        return carregarFoto(avaliado);
    }

    private static boolean temFoto(User usuario) {
        return usuario.isAtivo() && usuario.getFoto() != null && !usuario.getFoto().isBlank();
    }

    private ApiResponse carregarFoto(User usuario) {
        if (!temFoto(usuario)) {
            return new ApiResponse(false, "Usuário sem foto");
        }
        try {
            FotoStorageService.Foto foto = fotoStorageService.carregar(usuario.getFoto());
            return new ApiResponse(true, "Foto encontrada", foto);
        } catch (ResponseStatusException e) {
            if (e.getStatusCode().is5xxServerError()) {
                throw e;
            }
            return new ApiResponse(false, e.getReason());
        }
    }

    private record Participante(User usuario, Papel papel) {
    }
}
