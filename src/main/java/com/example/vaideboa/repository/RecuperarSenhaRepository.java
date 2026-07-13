package com.example.vaideboa.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.vaideboa.model.RecuperarSenha;
import com.example.vaideboa.model.User;

@Repository
public interface RecuperarSenhaRepository extends JpaRepository<RecuperarSenha,Long> {
    Optional<RecuperarSenha> findByUser(User user);
}
