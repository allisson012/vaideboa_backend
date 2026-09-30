package com.example.vaideboa.service;

import java.util.Arrays;
import java.util.HashMap;

import org.apache.tomcat.util.http.parser.MediaType;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.vaideboa.Dtos.RecomendacaoPrecoDto;
import com.example.vaideboa.model.Carro;

@Service 
public class RecomendacaoPrecoService {

    public void recomendacaoPreco(Double distancia, int quantPessoas, Carro carro, Long idCarro) {

        double consumoPadrao = 10.0; 
        double precoGasolina = 6.00; 
        carro.getVagas();
        double consumo = consumoPadrao;

        if (carro != null && carro.getConsumo() != null) {
            consumo = carro.getConsumo();
        }

        double litrosNecessarios = distancia / consumo;

        double custoCombustivel = litrosNecessarios * precoGasolina;

        double precoPessoa = custoCombustivel / quantPessoas;
        RecomendacaoPrecoDto dto = new RecomendacaoPrecoDto();
        dto.setConsumo(consumo);
        dto.setCustoCombustivel(custoCombustivel);
        dto.setDistancia(distancia);
        dto.setLitrosNecessarios(litrosNecessarios);
        dto.setPrecoGasolina(precoGasolina);
        dto.setPrecoPorPessoa(precoPessoa);
        dto.setQuantidadePessoas(quantPessoas);
        System.out.println("Custo total: R$ " + custoCombustivel);
        System.out.println("Preço por pessoa: R$ " + precoPessoa);
    }
}
