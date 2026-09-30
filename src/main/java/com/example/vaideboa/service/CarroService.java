package com.example.vaideboa.service;

import java.util.Optional;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.vaideboa.Dtos.ApiResponse;
import com.example.vaideboa.Dtos.CarroDto;
import com.example.vaideboa.Dtos.CarroRetornoDto;
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
        String placa = carroDto.getPlaca().toUpperCase().replaceAll("[^A-Z0-9]", "");
        if(carroRepository.existsByPlaca(placa)){
            return new ApiResponse(false, "Placa já cadastrada");
        }
        User user = userOpt.get();
        Carro carro = new Carro();
        carro.setAno(carroDto.getAno());
        carro.setArCondicionado(carroDto.getArCondicionado());
        carro.setAtivo(true);
        carro.setCor(carroDto.getCor());
        carro.setDescricao(carroDto.getDescricao());
        carro.setConsumo(carroDto.getConsumo());
        carro.setDono(user);
        carro.setFotoVeiculo(carroDto.getFotoVeiculo());
        carro.setMarca(carroDto.getMarca());
        carro.setModelo(carroDto.getModelo());
        carro.setPlaca(placa);
        carro.setVagas(carroDto.getVagas());
        carroRepository.save(carro);
        return new ApiResponse(true, "Carro cadastrado com sucesso!");
    }

    public ApiResponse meusCarros(String username) {
        Optional<User> userOpt = userRepository.findByUsernameAndAtivoTrue(username);
        if (userOpt.isEmpty()) {
            return new ApiResponse(false, "Usuário não encontrado");
        }

        List<CarroRetornoDto> carros = carroRepository.findByDonoAndAtivoTrue(userOpt.get())
            .stream()
            .map(carro -> CarroRetornoDto.from(carro, false))
            .toList();
        return new ApiResponse(true, "Carros encontrados com sucesso", carros);
    }
}
