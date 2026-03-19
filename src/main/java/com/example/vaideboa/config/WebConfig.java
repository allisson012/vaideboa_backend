package com.example.vaideboa.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")          // todas as rotas
                        .allowedOrigins("http://localhost:8081") // seu frontend
                        .allowedMethods("*")      // GET, POST, etc
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
        };
    }
}