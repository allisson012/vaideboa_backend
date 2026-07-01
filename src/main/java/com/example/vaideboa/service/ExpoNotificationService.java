package com.example.vaideboa.service;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.vaideboa.model.User;
import com.example.vaideboa.repository.UserRepository;


@Service
public class ExpoNotificationService {

    private final RestTemplate restTemplate;
    private final UserRepository userRepository;

    private static final String EXPO_API = "https://exp.host/--/api/v2/push/send";

    
    public ExpoNotificationService(RestTemplate restTemplate, UserRepository userRepository) {
        this.restTemplate = restTemplate;
        this.userRepository = userRepository;
    }


    public void enviar(String username, String titulo, String corpo) {
        Optional<User> userOpt = userRepository.findByUsernameAndAtivoTrue(username);
        if(userOpt.isEmpty()){
            return;
        }
        User user = userOpt.get();
        String expoPushToken  = user.getToken();

        if (expoPushToken == null || expoPushToken.isBlank()) {
            return;
        } 

         Map<String, Object> payload = new HashMap<>();
         payload.put("to", expoPushToken);
         payload.put("title", titulo);
         payload.put("body", corpo);
         payload.put("sound", "default");

         HttpHeaders headers = new HttpHeaders();
         headers.setContentType(MediaType.APPLICATION_JSON);

         HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
        try{
         restTemplate.postForObject(EXPO_API, request, String.class);
        } 
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}