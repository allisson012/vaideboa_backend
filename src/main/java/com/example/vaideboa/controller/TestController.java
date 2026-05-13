package com.example.vaideboa.controller;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.vaideboa.model.User;
import com.example.vaideboa.repository.UserRepository;
import com.example.vaideboa.service.GeoService;

@RestController
public class TestController {
    private final GeoService geoService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public TestController(GeoService geoService, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.geoService = geoService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/teste")
    public String teste() {
    for (int i = 1; i <= 50; i++) {

    User user = new User();

    user.setAtivo(true);
    user.setUsername("user" + i + "@gmail.com");
    String senha = passwordEncoder.encode("123456");
    user.setPassword(senha);

    userRepository.save(user);
    }
        return "ok";
    }

    @GetMapping("/testeGeoCode")
    public String testeGeoCode(){
        return geoService.reverseGeocode(-22.5866443, -44.9625176);
    }
}
