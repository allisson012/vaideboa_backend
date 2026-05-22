package com.example.vaideboa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.example.vaideboa.Dtos.ApiResponse;

@Service
public class EmailService {
    private final JavaMailSender mailSender;
    
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

  // ta funcionando no meu email pessoal tem que configurar senha de app para o email do vaideboa
    public ApiResponse enviarEmail(String mensagem,String assunto, String emailUsuario){
        SimpleMailMessage email = new SimpleMailMessage();

        email.setTo(emailUsuario);
        email.setSubject(assunto);
        email.setText(mensagem);

        mailSender.send(email);
        return null;
    }
}
