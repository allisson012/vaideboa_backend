package com.example.vaideboa.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.vaideboa.model.Faculdade;
import com.example.vaideboa.model.User;

public interface FaculdadeRepository extends JpaRepository<Faculdade, Long> {
    Optional<Faculdade> findFirstByUserOrderByIdAsc(User user);
}
