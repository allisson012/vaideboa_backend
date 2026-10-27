package com.example.vaideboa.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.vaideboa.model.Casa;

public interface CasaRepository extends JpaRepository<Casa, Long> {
}
