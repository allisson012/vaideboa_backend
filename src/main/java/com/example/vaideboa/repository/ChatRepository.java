package com.example.vaideboa.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.vaideboa.model.Chat;
import com.example.vaideboa.model.Reserva;

@Repository 
public interface ChatRepository extends JpaRepository<Chat, Long> {
    Optional<Chat> findByReserva(Reserva reserva);
}
