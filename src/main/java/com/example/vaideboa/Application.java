package com.example.vaideboa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling //-> deixei comentado para não ficar rodando schudeled durante desenvolvimento
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
