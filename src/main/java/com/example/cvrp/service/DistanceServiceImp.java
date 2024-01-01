package com.example.cvrp.service;

import com.example.cvrp.algorithms.CustomRoutingAlgorithm;
import com.example.cvrp.algorithms.DijkstraAlgorithm;
import com.example.cvrp.algorithms.RoutingAlgorithm;
import com.example.cvrp.dto.RouteLeg;
import com.example.cvrp.model.Address;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DistanceServiceImp {
    
    private final Map<String, RoutingAlgorithm> routingAlgorithms = new HashMap<>();
    private final GoogleMapsServiceImp googleMapsService;
    private final AddressService addressService;

    public DistanceServiceImp(GoogleMapsServiceImp googleMapsService, AddressService addressService) {
        this.googleMapsService = googleMapsService;
        this.addressService = addressService;
        // Initialize algorithms and add them to the map
        routingAlgorithms.put("custom", new CustomRoutingAlgorithm(googleMapsService));
        routingAlgorithms.put("dijkstra", new DijkstraAlgorithm(googleMapsService, addressService.findAllAddresses()));
        // Add other algorithms as needed
    }

    public List<RouteLeg> calculateRouteUsingAlgorithm(String algorithmType, int addressLimit, Long vehicleCapacity) {
        RoutingAlgorithm algorithm = routingAlgorithms.get(algorithmType);

        if (algorithm == null) {
            throw new IllegalArgumentException("Invalid algorithm type: " + algorithmType);
        }

        List<Address> addresses = addressService.findAllAddresses(addressLimit);
        return algorithm.calculateRoute(addresses, vehicleCapacity);
    }
}
