package com.example.vaideboa.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
public class GeoService {
    @Value("${api.key}")
    private String apiKey;

        public String reverseGeocode(double lat, double lon) {

        String url = "https://api.openrouteservice.org/geocode/reverse"
                + "?api_key=" + apiKey
                + "&point.lat=" + lat
                + "&point.lon=" + lon;

        RestTemplate restTemplate = new RestTemplate();
        String response = restTemplate.getForObject(url, String.class);

        try {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response);

        return root
            .path("features")
            .get(0)
            .path("properties")
            .path("label")
            .asText();

        } catch (Exception e) {
            return null;
        }
    }
}
