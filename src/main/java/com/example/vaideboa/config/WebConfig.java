package com.example.vaideboa.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig {
    @Value("${compartilhamento.publico.origens:http://localhost:5173}")
    private String[] origensCompartilhamento;


    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/compartilhamento/publico/**")
                        .allowedOrigins(origensCompartilhamento)
                        .allowedMethods("GET")
                        .allowCredentials(false);
                registry.addMapping("/**")      
                        .allowedOrigins("http://localhost:8081","http://127.0.0.1:5500")
                        .allowedMethods("GET", "POST", "PUT", "DELETE")
                        .allowedHeaders("Authorization", "Content-Type")
                        .allowCredentials(true);
            }
        };
    }
}