package com.example.vaideboa.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.vaideboa.model.Casa;
import com.example.vaideboa.model.User;

public interface CasaRepository extends JpaRepository<Casa, Long> {
    Optional<Casa> findFirstByUserOrderByIdAsc(User user);
}
