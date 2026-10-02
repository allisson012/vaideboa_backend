package com.example.vaideboa.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.vaideboa.Dtos.ApiResponse;
import com.example.vaideboa.Dtos.MensagemDto;
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



    public ChatService(ChatRepository chatRepository, UserRepository userRepository,
            ReservaRepository reservaRepository, MensagemRepository mensagemRepository) {
        this.chatRepository = chatRepository;
        this.userRepository = userRepository;
        this.reservaRepository = reservaRepository;
        this.mensagemRepository = mensagemRepository;
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
        if(!reserva.getPassageiro().getId().equals(user.getId()) && !reserva.getCarona().getMotorista().getId().equals(user.getId())){
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
        mensagemRepository.save(mensagem);
        return new ApiResponse(true, "Mensagem enviada com sucesso");
    }

    public void conectarChat(){

    }

    public void buscarMensagens(){
        
    }
}
