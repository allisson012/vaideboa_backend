package com.example.vaideboa.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.springframework.stereotype.Service;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
public class RotaMapperService {

public LineString extrairLineString(String json) {
    try {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(json);

        JsonNode coords = root
                .path("features")
                .get(0)
                .path("geometry")
                .path("coordinates");

        GeometryFactory factory = new GeometryFactory();
        Coordinate[] coordinates = new Coordinate[coords.size()];

        for (int i = 0; i < coords.size(); i++) {
            double lon = coords.get(i).get(0).asDouble();
            double lat = coords.get(i).get(1).asDouble();

            coordinates[i] = new Coordinate(lon, lat);
        }

        return factory.createLineString(coordinates);

    } catch (Exception e) {
        throw new RuntimeException(e);
    }
}

public List<Map<String, Double>> toReactNative(LineString line) {
    List<Map<String, Double>> coords = new ArrayList<>();

    for (Coordinate c : line.getCoordinates()) {
        Map<String, Double> point = new HashMap<>();
        point.put("latitude", c.y);
        point.put("longitude", c.x);
        coords.add(point);
    }

    return coords;
}

}
