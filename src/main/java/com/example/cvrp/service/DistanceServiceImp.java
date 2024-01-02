package com.example.cvrp.service;

import com.example.cvrp.algorithms.CustomRoutingAlgorithm;
import com.example.cvrp.algorithms.DijkstraAlgorithm;
import com.example.cvrp.algorithms.RoutingAlgorithm;
import com.example.cvrp.dto.RouteLeg;
import com.example.cvrp.model.Address;
import com.example.cvrp.model.GoogleMapsResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DistanceServiceImp {
    private final GoogleMapsServiceImp googleMapsService;
    private final AddressService addressService;
    private final Map<String, RoutingAlgorithm> routingAlgorithms;

    public DistanceServiceImp(GoogleMapsServiceImp googleMapsService, AddressService addressService) {
        this.googleMapsService = googleMapsService;
        this.addressService = addressService;

        routingAlgorithms = new HashMap<>();
        routingAlgorithms.put("Custom", new CustomRoutingAlgorithm(googleMapsService));
        routingAlgorithms.put("Dijkstra", new DijkstraAlgorithm(googleMapsService, addressService.findAllAddresses()));
    }

    public List<RouteLeg> calculateOptimalRoute(String algorithmType, int addressLimit, Long vehicleCapacity) {
        RoutingAlgorithm selectedAlgorithm = routingAlgorithms.get(algorithmType);
        if (selectedAlgorithm != null) {
            List<Address> addresses = addressService.findAllAddresses(addressLimit + 1);
            return selectedAlgorithm.calculateRoute(addresses, vehicleCapacity);
        } else {
            throw new IllegalArgumentException("Unknown routing algorithm: " + algorithmType);
        }
    }
}

