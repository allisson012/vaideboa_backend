package com.example.vaideboa.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.vaideboa.Dtos.ApiResponse;
import com.example.vaideboa.Dtos.CarroDto;
import com.example.vaideboa.model.Carro;
import com.example.vaideboa.model.User;
import com.example.vaideboa.repository.CarroRepository;
import com.example.vaideboa.repository.UserRepository;

@Service
public class CarroService {
    private final UserRepository userRepository;
    private final CarroRepository carroRepository;

    public CarroService(UserRepository userRepository, CarroRepository carroRepository) {
        this.userRepository = userRepository;
        this.carroRepository = carroRepository;
    }

    public ApiResponse cadastrarCarro(String username, CarroDto carroDto){
        Optional<User> userOpt = userRepository.findByUsernameAndAtivoTrue(username);
        if(userOpt.isEmpty()){
            return new ApiResponse(false, "Usuário não encontrado");
        }
        if(carroRepository.existsByPlaca(carroDto.getPlaca())){
            return new ApiResponse(false, "Placa já cadastrada");
        }
        User user = userOpt.get();
        Carro carro = new Carro();
        carro.setAno(carroDto.getAno());
        carro.setArCondicionado(carroDto.getArCondicionado());
        carro.setAtivo(true);
        carro.setCor(carroDto.getCor());
        carro.setDescricao(carroDto.getDescricao());
        carro.setDono(user);
        carro.setFotoVeiculo(carroDto.getFotoVeiculo());
        carro.setMarca(carroDto.getMarca());
        carro.setModelo(carroDto.getModelo());
        carro.setPlaca(carroDto.getPlaca());
        carro.setVagas(carroDto.getVagas());
        carroRepository.save(carro);
        return new ApiResponse(true, "Carro cadastrado com sucesso!");
    }
}
