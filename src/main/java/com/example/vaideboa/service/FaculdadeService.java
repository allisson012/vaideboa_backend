package com.example.vaideboa.service;

import java.util.Optional;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public ApiResponse cadastrarFaculdade(String username, FaculdadeDto dto) {
        // O bloqueio do usuário também protege o primeiro cadastro contra requisições simultâneas.
        Optional<User> userOpt = userRepository.buscarAtivoPorUsernameComBloqueio(username);
        if (userOpt.isEmpty()) {
            return new ApiResponse(false, "Usuário não encontrado ou inativo");
        }
        User user = userOpt.get();
        Optional<Faculdade> faculdadeOpt = faculdadeRepository.findFirstByUserOrderByIdAsc(user);
        Faculdade faculdade = faculdadeOpt.orElseGet(Faculdade::new);
        faculdade.setDescrisao(dto.getDescricao());
        faculdade.setLocalizacao(geometryFactory.createPoint(new Coordinate(dto.getLongitude(), dto.getLatitude())));
        faculdade.setUser(user);
        faculdadeRepository.save(faculdade);

        return new ApiResponse(true, faculdadeOpt.isPresent()
                ? "Faculdade atualizada com sucesso!" : "Faculdade cadastrada com sucesso!");
    }
}
