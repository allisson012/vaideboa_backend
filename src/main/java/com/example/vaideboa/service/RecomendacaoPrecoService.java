package com.example.vaideboa.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.vaideboa.Dtos.ApiResponse;
import com.example.vaideboa.Dtos.RecomendacaoPrecoDto;
import com.example.vaideboa.model.Carro;
import com.example.vaideboa.repository.CarroRepository;

@Service 
public class RecomendacaoPrecoService {
    private final CarroRepository carroRepository;

    public RecomendacaoPrecoService(CarroRepository carroRepository) {
        this.carroRepository = carroRepository;
    }


    public ApiResponse recomendacaoPreco(Double distancia, Integer quantPessoas, Long idCarro, String username) {

        if (distancia == null || !Double.isFinite(distancia) || distancia <= 0) {
            return new ApiResponse(false, "A distância deve ser um número maior que zero, em quilômetros");
        }
        if (quantPessoas == null || quantPessoas <= 0) {
            return new ApiResponse(false, "A quantidade de pessoas deve ser maior que zero e incluir o motorista");
        }
        if (idCarro == null || idCarro <= 0) {
            return new ApiResponse(false, "Informe um ID de carro válido");
        }

        double consumoPadrao = 10.0; 
        double precoGasolina = 6.00; 
        
        double consumo = consumoPadrao;
        Optional<Carro> carroOpt = carroRepository.findById(idCarro);
        if (carroOpt.isEmpty()) {
            return new ApiResponse(false, "Carro não encontrado");
        }
        Carro carro = carroOpt.get();
        if (!Boolean.TRUE.equals(carro.getAtivo()) || carro.getDono() == null || username == null || !username.equals(carro.getDono().getUsername())) {
            return new ApiResponse(false, "Carro indisponível para este usuário");
        }
        if (carro.getConsumo() != null) {
            consumo = carro.getConsumo();
        }
        if (!Double.isFinite(consumo) || consumo <= 0) {
            return new ApiResponse(false, "O consumo do carro deve ser maior que zero, em km/l");
        }

        double litrosNecessarios = distancia / consumo;

        double custoCombustivel = litrosNecessarios * precoGasolina;
        if (!Double.isFinite(custoCombustivel)) {
            return new ApiResponse(false, "Os valores informados excedem o limite do cálculo");
        }

        double precoPessoa = custoCombustivel / quantPessoas;
        RecomendacaoPrecoDto dto = new RecomendacaoPrecoDto();
        dto.setConsumo(consumo);
        dto.setCustoCombustivel(custoCombustivel);
        dto.setPrecoTotal(custoCombustivel);
        dto.setDistancia(distancia);
        dto.setLitrosNecessarios(litrosNecessarios);
        dto.setPrecoGasolina(precoGasolina);
        dto.setPrecoPorPessoa(precoPessoa);
        dto.setQuantidadePessoas(quantPessoas);
        return new ApiResponse(true,"Recomendação de preço calculada com sucesso",dto);
    }
}
