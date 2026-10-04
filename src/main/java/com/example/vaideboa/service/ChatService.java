package com.example.vaideboa.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.vaideboa.Dtos.ApiResponse;
import com.example.vaideboa.Dtos.MensagemDto;
import com.example.vaideboa.Dtos.MensagemRetornoDto;
import com.example.vaideboa.Dtos.MensagensPaginaDto;
import com.example.vaideboa.event.MensagemCriadaEvent;
import com.example.vaideboa.model.Chat;
import com.example.vaideboa.model.Mensagem;
import com.example.vaideboa.model.Reserva;
import com.example.vaideboa.model.User;
import com.example.vaideboa.model.enums.StatusCarona;
import com.example.vaideboa.model.enums.StatusReserva;
import com.example.vaideboa.repository.ChatRepository;
import com.example.vaideboa.repository.MensagemRepository;
import com.example.vaideboa.repository.ReservaRepository;
import com.example.vaideboa.repository.UserRepository;

@Service 
public class ChatService {
    private final ChatRepository chatRepository;
    private final UserRepository userRepository;
    private final ReservaRepository reservaRepository;
    private final MensagemRepository mensagemRepository;
    private final ApplicationEventPublisher eventPublisher;



    public ChatService(ChatRepository chatRepository, UserRepository userRepository,
            ReservaRepository reservaRepository, MensagemRepository mensagemRepository,
            ApplicationEventPublisher eventPublisher) {
        this.chatRepository = chatRepository;
        this.userRepository = userRepository;
        this.reservaRepository = reservaRepository;
        this.mensagemRepository = mensagemRepository;
        this.eventPublisher = eventPublisher;
    }
    public ApiResponse iniciarChat(Long idReserva, String username){
        return null;
    }
    public void iniciarChat(Reserva reserva){
        Chat chat = new Chat();
        chat.setCriadoEm(LocalDateTime.now());
        chat.setReserva(reserva);
        chatRepository.save(chat);
    }
    @Transactional
    public ApiResponse enviarMensagem(String username, MensagemDto mensagemDto){
        Optional<User> userOpt = userRepository.findByUsernameAndAtivoTrue(username);
        if(userOpt.isEmpty()){
            return new ApiResponse(false, "Usuário não encontrado");
        }
        User user = userOpt.get();
        Optional<Reserva> reservaOpt = reservaRepository.findById(mensagemDto.getIdReserva());
        if(reservaOpt.isEmpty()){
            return new ApiResponse(false, "Reserva não encontrada");
        }
        Reserva reserva = reservaOpt.get();
        if(!participaDaReserva(user, reserva)){
            return new ApiResponse(false, "Usuário não faz parte dessa reserva");
        }

        if(StatusCarona.CANCELADA.equals(reserva.getCarona().getStatusCarona())){
            return new ApiResponse(false, "Não é possível enviar mensagens em uma carona cancelada");
        }

        if(StatusReserva.PENDENTE.equals(reserva.getStatusReserva())){
            return new ApiResponse(false,"Não é possivel iniciar conversa antes do motorista aceitar pedido");
        }

        Optional<Chat> chatOpt = chatRepository.findByReserva(reserva);
        if(chatOpt.isEmpty()){
            return new ApiResponse(false,"Não foi possivel encontrar o chat");
        }
        Chat chat = chatOpt.get();

        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime concluidaEm = reserva.getCarona().getConcluidaEm();
        if(concluidaEm != null && agora.isAfter(concluidaEm.plusDays(2))){
            return new ApiResponse(false, "Não é possível enviar mensagens após dois dias da conclusão da carona");
        } 
        Mensagem mensagem = new Mensagem();
        mensagem.setAutor(user);
        mensagem.setChat(chat);
        mensagem.setEnviadoEm(agora);
        mensagem.setMensagem(mensagemDto.getMensagem());
        Mensagem salva = mensagemRepository.save(mensagem);
        MensagemRetornoDto retorno = MensagemRetornoDto.from(salva);
        eventPublisher.publishEvent(new MensagemCriadaEvent(retorno));
        return new ApiResponse(true, "Mensagem enviada com sucesso", retorno);
    }

    @Transactional(readOnly = true)
    public boolean podeAcessarChat(String username, Long idReserva) {
        Optional<User> userOpt = userRepository.findByUsernameAndAtivoTrue(username);
        Optional<Reserva> reservaOpt = reservaRepository.findById(idReserva);
        if (userOpt.isEmpty() || reservaOpt.isEmpty()) {
            return false;
        }
        Reserva reserva = reservaOpt.get();
        return participaDaReserva(userOpt.get(), reserva) && chatRepository.findByReserva(reserva).isPresent();
    }

    @Transactional(readOnly = true)
    public MensagensPaginaDto buscarMensagens(String username, Long idReserva, int page, int size) {
        if (idReserva == null || idReserva <= 0 || page < 0 || size < 1 || size > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Informe uma reserva positiva, page maior ou igual a zero e size entre 1 e 100");
        }
        User user = userRepository.findByUsernameAndAtivoTrue(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Usuário não encontrado ou inativo"));
        Reserva reserva = reservaRepository.findById(idReserva)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Reserva não encontrada"));
        if (!participaDaReserva(user, reserva)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Usuário não faz parte dessa reserva");
        }
        Chat chat = chatRepository.findByReserva(reserva)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Chat não encontrado"));

        return MensagensPaginaDto.from(mensagemRepository
                .findByChatOrderByEnviadoEmDescIdDesc(chat, PageRequest.of(page, size))
                .map(MensagemRetornoDto::from));
    }

    private boolean participaDaReserva(User user, Reserva reserva) {
        return reserva.getPassageiro().getId().equals(user.getId()) || reserva.getCarona().getMotorista().getId().equals(user.getId());
    }
}
