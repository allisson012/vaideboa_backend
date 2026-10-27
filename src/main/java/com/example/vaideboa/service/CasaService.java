package com.example.vaideboa.service;

import java.util.Optional;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Service;

import com.example.vaideboa.Dtos.ApiResponse;
import com.example.vaideboa.Dtos.CasaDto;
import com.example.vaideboa.model.Casa;
import com.example.vaideboa.model.User;
import com.example.vaideboa.repository.CasaRepository;
import com.example.vaideboa.repository.UserRepository;

@Service
public class CasaService {
    private final UserRepository userRepository;
    private final CasaRepository casaRepository;
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    public CasaService(UserRepository userRepository, CasaRepository casaRepository) {
        this.userRepository = userRepository;
        this.casaRepository = casaRepository;
    }

    public ApiResponse cadastrarCasa(String username, CasaDto dto) {
        Optional<User> userOpt = userRepository.findByUsernameAndAtivoTrue(username);
        if (userOpt.isEmpty()) {
            return new ApiResponse(false, "Usuário não encontrado ou inativo");
        }
        User user = userOpt.get();
        Casa casa = new Casa();
        casa.setDescricao(dto.getDescricao());
        casa.setLocalizacao(geometryFactory.createPoint(new Coordinate(dto.getLongitude(), dto.getLatitude())));
        casa.setUser(user);
        casaRepository.save(casa);

        return new ApiResponse(true, "Casa cadastrada com sucesso!");
    }
}
