package com.example.vaideboa.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.vaideboa.Dtos.ApiResponse;
import com.example.vaideboa.Dtos.RecuperarSenhaDto;
import com.example.vaideboa.model.RecuperarSenha;
import com.example.vaideboa.model.User;
import com.example.vaideboa.repository.RecuperarSenhaRepository;
import com.example.vaideboa.repository.UserRepository;
import com.example.vaideboa.security.JwtService;
import com.example.vaideboa.validator.SenhaValidator;

@Service
public class RecuperacaoSenhaService {
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final RecuperarSenhaRepository recuperarSenhaRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public RecuperacaoSenhaService(UserRepository userRepository, EmailService emailService,
            RecuperarSenhaRepository recuperarSenhaRepository, JwtService jwtService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.recuperarSenhaRepository = recuperarSenhaRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    public ApiResponse enviarEmailRecuperacao(String email){
        Optional<User> userOpt = userRepository.findByUsernameAndAtivoTrue(email);
        if(userOpt.isEmpty()){
            // para evitar expor se existe esse email no sistema
            return new ApiResponse(true,"Se existir uma conta vinculada a este e-mail, um código de recuperação será enviado."); 
        }
        User user = userOpt.get();

        Optional<RecuperarSenha> recuperarSenhaOpt = recuperarSenhaRepository.findByUser(user);
        RecuperarSenha recuperarSenha;
        // criar uma de historico pq aqui eu sobrescrevo perco a informação de quantas vezes pediu etc
        // então posso criar uma tabela que salve os registro para ter um historico de se o usuario esta 
        // abusando da funcionalidade 
        if (recuperarSenhaOpt.isPresent()) {
            recuperarSenha = recuperarSenhaOpt.get();
        } else {
            recuperarSenha = new RecuperarSenha();
            recuperarSenha.setUser(user);
        }
        SecureRandom random = new SecureRandom();
        // gera um codigo de 6 digitos
        String codigo = String.format("%06d", random.nextInt(1000000));
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String codigoHash = encoder.encode(codigo);

        // transformar em hash esse codigo ta faltando
        recuperarSenha.setUser(user);
        recuperarSenha.setTokenHash(codigoHash);
        recuperarSenha.setExpiresAt(LocalDateTime.now().plusMinutes(15));
        
        String assunto = "🔐 Recuperação de senha - VaiDeBoa";

        String mensagem = """
            Olá, %s!

            Recebemos uma solicitação para redefinir a senha da sua conta no VaiDeBoa.

            Seu código de verificação é:

            🔑 %s

            Este código é válido por 15 minutos.

            Caso você não tenha solicitado a recuperação da senha, ignore este e-mail. Sua conta continuará protegida e nenhuma alteração será realizada.

            Por segurança:
            • Nunca compartilhe este código com outras pessoas.
            • A equipe do VaiDeBoa nunca solicitará esse código.

            Atenciosamente,

            Equipe VaiDeBoa
        """.formatted(
            user.getNome(),
            codigo
        );
        recuperarSenhaRepository.save(recuperarSenha);
        emailService.enviarEmail(mensagem, assunto, email);
        return new ApiResponse(true,"Se existir uma conta vinculada a este e-mail, um código de recuperação será enviado."); 
      
    }

    public ApiResponse validarCodigo(String codigo, String email){
        Optional<User> userOpt = userRepository.findByUsernameAndAtivoTrue(email);
        if(userOpt.isEmpty()){
            return new ApiResponse(false,"Código inválido.");
        }
        User user = userOpt.get();
        Optional<RecuperarSenha> recuperarSenhaOpt = recuperarSenhaRepository.findByUser(user);
        if(recuperarSenhaOpt.isEmpty()){
            return new ApiResponse(false,"Solicitação de recuperação não encontrada.");
        }
        RecuperarSenha recuperarSenha = recuperarSenhaOpt.get();
        if(recuperarSenha.getUtilizado()){
            return new ApiResponse(false,"Este código já foi utilizado. Solicite um novo código.");
        }
        LocalDateTime agora = LocalDateTime.now();
        if(!recuperarSenha.getExpiresAt().isAfter(agora)){
            return new ApiResponse(false,"O código de recuperação expirou. Solicite um novo código.");
        }
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        if (!encoder.matches(codigo, recuperarSenha.getTokenHash())) {
            return new ApiResponse(false, "Código inválido.");
        }
        String tokenReset = jwtService.generatePasswordResetToken(recuperarSenha);
        return new ApiResponse(true,"Codigo validado com sucesso",tokenReset);
    }

    public ApiResponse alterarSenha(RecuperarSenhaDto dto){
        boolean valido = jwtService.isPasswordResetToken(dto.getTokenReset());
        if(!valido){
            return new ApiResponse(false,"Não foi possível validar a solicitação de recuperação de senha.");
        }
        Long id = jwtService.getRecoveryId(dto.getTokenReset());
        Optional<RecuperarSenha> recuperarSenhaOpt = recuperarSenhaRepository.findById(id);
        if(recuperarSenhaOpt.isEmpty()){
            return new ApiResponse(false,"Não foi possível validar a solicitação de recuperação de senha.");
        }
        RecuperarSenha recuperarSenha = recuperarSenhaOpt.get();
        String email = jwtService.getEmail(dto.getTokenReset());
        User user = recuperarSenha.getUser();
        if(!email.equals(user.getUsername())){
            return new ApiResponse(false, "Não foi possível validar a solicitação de recuperação de senha.");
        }
        if(recuperarSenha.getUtilizado()){
            return new ApiResponse(false, "Esta solicitação de recuperação já foi utilizada.");
        }
        LocalDateTime agora = LocalDateTime.now();
        if(!recuperarSenha.getExpiresAt().isAfter(agora)){
            return new ApiResponse(false,"O código de recuperação expirou. Solicite um novo código.");
        }
        if(!SenhaValidator.senhaValida(dto.getSenha()))
        {
            return new ApiResponse(false,"A senha não atende aos requisitos mínimos de segurança.");
        }
        if (!dto.getSenha().equals(dto.getConfirmarSenha())) {
            return new ApiResponse(false, "As senhas informadas não coincidem.");
        }
        if (passwordEncoder.matches(dto.getSenha(), user.getPassword())) {
            return new ApiResponse(false,"A nova senha deve ser diferente da senha atual.");
        }
        String novaSenha = passwordEncoder.encode(dto.getSenha());
        user.setPassword(novaSenha);
        userRepository.save(user);
        recuperarSenha.setUtilizado(true);
        recuperarSenhaRepository.save(recuperarSenha);

        return new ApiResponse(true,"Senha alterada com sucesso");
    }
    // gerar um jwt de recuperação 
}
