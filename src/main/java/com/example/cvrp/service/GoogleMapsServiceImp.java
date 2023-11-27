package com.example.cvrp.service;

import com.example.cvrp.model.GoogleMapsResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class GoogleMapsServiceImp {

    private final RestTemplate restTemplate;
    private final String googleMapsApiKey;

    public GoogleMapsServiceImp(@Value("${google.maps.apikey}") String googleMapsApiKey) {
        this.restTemplate = new RestTemplate();
        this.googleMapsApiKey = googleMapsApiKey;
    }

    public GoogleMapsResponse getDistanceMatrix(String origins, String destinations) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder
                .fromHttpUrl("https://maps.googleapis.com/maps/api/distancematrix/json")
                .queryParam("origins", origins)
                .queryParam("destinations", destinations)
                .queryParam("key", googleMapsApiKey);

        return restTemplate.getForObject(uriBuilder.toUriString(), GoogleMapsResponse.class);
    }
}
