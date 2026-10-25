package com.example.vaideboa.service;

import java.util.Optional;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Service;

import com.example.vaideboa.Dtos.ApiResponse;
import com.example.vaideboa.Dtos.FaculdadeDto;
import com.example.vaideboa.model.Faculdade;
import com.example.vaideboa.model.User;
import com.example.vaideboa.repository.FaculdadeRepository;
import com.example.vaideboa.repository.UserRepository;

@Service
public class FaculdadeService {
    private final UserRepository userRepository;
    private final FaculdadeRepository faculdadeRepository;
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    public FaculdadeService(UserRepository userRepository, FaculdadeRepository faculdadeRepository) {
        this.userRepository = userRepository;
        this.faculdadeRepository = faculdadeRepository;
    }

    public ApiResponse cadastrarFaculdade(String username, FaculdadeDto dto) {
        Optional<User> userOpt = userRepository.findByUsernameAndAtivoTrue(username);
        if (userOpt.isEmpty()) {
            return new ApiResponse(false, "Usuário não encontrado ou inativo");
        }
        User user = userOpt.get();
        Faculdade faculdade = new Faculdade();
        faculdade.setDescrisao(dto.getDescricao());
        faculdade.setLocalizacao(geometryFactory.createPoint(new Coordinate(dto.getLongitude(), dto.getLatitude())));
        faculdade.setUser(user);
        faculdadeRepository.save(faculdade);

        return new ApiResponse(true, "Faculdade cadastrada com sucesso!");
    }
}
