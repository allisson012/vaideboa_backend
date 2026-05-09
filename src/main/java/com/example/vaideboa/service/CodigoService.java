package com.example.vaideboa.service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.example.vaideboa.Dtos.ApiResponse;
import com.example.vaideboa.model.Reserva;
import com.example.vaideboa.model.ReservaCodigo;
import com.example.vaideboa.repository.ReservaCodigoRepository;
import com.example.vaideboa.repository.ReservaRepository;

@Service
public class CodigoService {
    private final ReservaCodigoRepository reservaCodigoRepository;
    private final ReservaRepository reservaRepository;
    
    public CodigoService(ReservaCodigoRepository reservaCodigoRepository, ReservaRepository reservaRepository) {
        this.reservaCodigoRepository = reservaCodigoRepository;
        this.reservaRepository = reservaRepository;
    }

    public ApiResponse gerarCodigos(List<Reserva> reservas){
        Random random = new Random();

        try {
            List<ReservaCodigo> codigos = new ArrayList<>();
            Set<String> usados = new HashSet<>();

            for (Reserva reserva : reservas) {

                String codigoEmbarque;
                String codigoDesembarque;

                do {
                    codigoEmbarque = String.format("%04d", random.nextInt(10000));
                } while (usados.contains(codigoEmbarque));

                usados.add(codigoEmbarque);

                do {
                    codigoDesembarque = String.format("%04d", random.nextInt(10000));
                } while (usados.contains(codigoDesembarque));

                usados.add(codigoDesembarque);

                ReservaCodigo reservaCodigo = new ReservaCodigo();
                reservaCodigo.setCodigoEmbarque(codigoEmbarque);
                reservaCodigo.setCodigoDesembarque(codigoDesembarque);
                reservaCodigo.setCriadoEm(LocalDateTime.now());

                codigos.add(reservaCodigo);

                reserva.setJaEnviadoCodigoInicio(true);
            }

            reservaCodigoRepository.saveAll(codigos);
            reservaRepository.saveAll(reservas);

            return new ApiResponse(true, "Códigos gerados com sucesso");

        }catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse(false, "Erro ao gerar códigos");
        }
    }
}
