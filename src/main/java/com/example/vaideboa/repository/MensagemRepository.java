package com.example.vaideboa.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.vaideboa.model.Mensagem;
import com.example.vaideboa.model.Chat;

@Repository 
public interface MensagemRepository extends JpaRepository<Mensagem, Long> {
    Page<Mensagem> findByChatOrderByEnviadoEmDescIdDesc(Chat chat, Pageable pageable);
}
