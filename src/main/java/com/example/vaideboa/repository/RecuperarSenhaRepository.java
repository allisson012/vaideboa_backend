package com.example.vaideboa.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;

import com.example.vaideboa.model.RecuperarSenha;
import com.example.vaideboa.model.User;

@Repository
public interface RecuperarSenhaRepository extends JpaRepository<RecuperarSenha,Long> {
    // Bloqueia o usuário mesmo quando ainda não existe uma solicitação de recuperação.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from User u where u.id = :id")
    Optional<User> bloquearUsuario(@Param("id") Long id);

    Optional<RecuperarSenha> findByUser(User user);
}
