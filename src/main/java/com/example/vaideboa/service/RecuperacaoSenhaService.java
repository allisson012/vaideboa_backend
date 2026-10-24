package com.example.vaideboa.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public ApiResponse enviarEmailRecuperacao(String email){
        Optional<User> userOpt = userRepository.findByUsernameAndAtivoTrue(email);
        if(userOpt.isEmpty()){
            return new ApiResponse(true,"Se existir uma conta vinculada a este e-mail, um código de recuperação será enviado."); 
        }
        User user = recuperarSenhaRepository.bloquearUsuario(userOpt.get().getId()).orElseThrow();
        if (!user.isAtivo()) {
            return new ApiResponse(true, "Se existir uma conta vinculada a este e-mail, um código de recuperação será enviado.");
        }

        Optional<RecuperarSenha> recuperarSenhaOpt = recuperarSenhaRepository.findByUser(user);
        RecuperarSenha recuperarSenha;
        if (recuperarSenhaOpt.isPresent()) {
            recuperarSenha = recuperarSenhaOpt.get();
        } else {
            recuperarSenha = new RecuperarSenha();
            recuperarSenha.setUser(user);
        }
        LocalDateTime agora = LocalDateTime.now();
        if (recuperarSenha.getBloqueadoAte() != null && recuperarSenha.getBloqueadoAte().isAfter(agora)) {
            return new ApiResponse(true, "Se existir uma conta vinculada a este e-mail, um código de recuperação será enviado.");
        }
        if (!Boolean.TRUE.equals(recuperarSenha.getUtilizado()) && !Boolean.TRUE.equals(recuperarSenha.getBloqueado()) && recuperarSenha.getExpiresAt() != null && recuperarSenha.getExpiresAt().isAfter(agora)) {
            return new ApiResponse(true, "Se existir uma conta vinculada a este e-mail, um código de recuperação será enviado.");
        }
        SecureRandom random = new SecureRandom();
        // gera um codigo de 6 digitos
        String codigo = String.format("%06d", random.nextInt(1000000));
        String codigoHash = passwordEncoder.encode(codigo);

        recuperarSenha.setUser(user);
        recuperarSenha.setTokenHash(codigoHash);
        recuperarSenha.setExpiresAt(agora.plusMinutes(15));
        recuperarSenha.setTentativasFalhas(0);
        recuperarSenha.setBloqueado(false);
        recuperarSenha.setBloqueadoAte(null);
        recuperarSenha.setUtilizado(false);
        
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

    @Transactional
    public ApiResponse validarCodigo(String codigo, String email){
        Optional<User> userOpt = userRepository.findByUsernameAndAtivoTrue(email);
        if(userOpt.isEmpty()){
            return new ApiResponse(false,"Código inválido.");
        }
        User user = recuperarSenhaRepository.bloquearUsuario(userOpt.get().getId()).orElseThrow();
        if (!user.isAtivo()) {
            return new ApiResponse(false, "Código inválido.");
        }
        Optional<RecuperarSenha> recuperarSenhaOpt = recuperarSenhaRepository.findByUser(user);
        if(recuperarSenhaOpt.isEmpty()){
            return new ApiResponse(false,"Solicitação de recuperação não encontrada.");
        }
        RecuperarSenha recuperarSenha = recuperarSenhaOpt.get();
        if(Boolean.TRUE.equals(recuperarSenha.getUtilizado())){
            return new ApiResponse(false,"Este código já foi utilizado. Solicite um novo código.");
        }
        LocalDateTime agora = LocalDateTime.now();
        if (recuperarSenha.getBloqueadoAte() != null && recuperarSenha.getBloqueadoAte().isAfter(agora)) {
            return new ApiResponse(false, "Limite de tentativas atingido. Aguarde o fim do bloqueio para solicitar um novo código.");
        }
        if (Boolean.TRUE.equals(recuperarSenha.getBloqueado()) || recuperarSenha.getTentativasFalhas() >= 6) {
            return new ApiResponse(false, "Este código foi bloqueado. Solicite um novo código.");
        }
        if(recuperarSenha.getExpiresAt() == null || !recuperarSenha.getExpiresAt().isAfter(agora)){
            return new ApiResponse(false,"O código de recuperação expirou. Solicite um novo código.");
        }
        if (codigo == null || !codigo.matches("[0-9]{6}") || !passwordEncoder.matches(codigo, recuperarSenha.getTokenHash())) { 
            recuperarSenha.setTentativasFalhas(recuperarSenha.getTentativasFalhas() + 1);
            if (recuperarSenha.getTentativasFalhas() >= 6) {
                recuperarSenha.setBloqueadoAte(agora.plusMinutes(15));
                recuperarSenha.setBloqueado(true);
            }
            recuperarSenhaRepository.save(recuperarSenha);
            if (Boolean.TRUE.equals(recuperarSenha.getBloqueado())) {
                return new ApiResponse(false, "Limite de tentativas atingido. Aguarde 15 minutos para solicitar um novo código.");
            }
            return new ApiResponse(false, "Código inválido.");
        }
        String tokenReset = jwtService.generatePasswordResetToken(recuperarSenha);
        return new ApiResponse(true,"Codigo validado com sucesso",tokenReset);
    }

    @Transactional
    public ApiResponse alterarSenha(RecuperarSenhaDto dto){
        boolean valido = jwtService.isPasswordResetToken(dto.getTokenReset());
        if(!valido){
            return new ApiResponse(false,"Não foi possível validar a solicitação de recuperação de senha.");
        }
        Long id = jwtService.getRecoveryId(dto.getTokenReset());
        String email = jwtService.getEmail(dto.getTokenReset());
        Optional<User> userOpt = userRepository.findByUsernameAndAtivoTrue(email);
        if (userOpt.isEmpty()) {
            return new ApiResponse(false, "Não foi possível validar a solicitação de recuperação de senha.");
        }
        User user = recuperarSenhaRepository.bloquearUsuario(userOpt.get().getId()).orElseThrow();
        Optional<RecuperarSenha> recuperarSenhaOpt = recuperarSenhaRepository.findById(id);
        if(recuperarSenhaOpt.isEmpty()){
            return new ApiResponse(false,"Não foi possível validar a solicitação de recuperação de senha.");
        }
        RecuperarSenha recuperarSenha = recuperarSenhaOpt.get();
        if (!user.isAtivo() || !recuperarSenha.getUser().getId().equals(user.getId())) {
            return new ApiResponse(false, "Não foi possível validar a solicitação de recuperação de senha.");
        }
        if(Boolean.TRUE.equals(recuperarSenha.getUtilizado())){
            return new ApiResponse(false, "Esta solicitação de recuperação já foi utilizada.");
        }
        LocalDateTime agora = LocalDateTime.now();
        if (Boolean.TRUE.equals(recuperarSenha.getBloqueado()) || recuperarSenha.getTentativasFalhas() >= 6 || (recuperarSenha.getBloqueadoAte() != null && recuperarSenha.getBloqueadoAte().isAfter(agora))) {
            return new ApiResponse(false, "Esta solicitação foi bloqueada. Solicite um novo código após o fim do bloqueio.");
        }
        if(recuperarSenha.getExpiresAt() == null || !recuperarSenha.getExpiresAt().isAfter(agora)){
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
