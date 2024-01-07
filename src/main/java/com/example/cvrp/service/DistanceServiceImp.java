package com.example.cvrp.service;

import com.example.cvrp.algorithms.*;
import com.example.cvrp.dto.RouteLeg;
import com.example.cvrp.model.Address;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DistanceServiceImp {
    private final GoogleMapsServiceImp googleMapsService;
    private final AddressService addressService;
    private final Map<String, RoutingAlgorithm> routingAlgorithms;

    public DistanceServiceImp(GoogleMapsServiceImp googleMapsService, AddressService addressService) {
        this.googleMapsService = googleMapsService;
        this.addressService = addressService;

        routingAlgorithms = new HashMap<>();
        routingAlgorithms.put("NearestNeighbor", new NearestNeighborAlgorithm(googleMapsService));
        //routingAlgorithms.put("Dijkstra", new DijkstraAlgorithm(googleMapsService));
        routingAlgorithms.put("SimulatedAnnealing", new SimulatedAnnealingAlgorithm(googleMapsService));
        routingAlgorithms.put("Savings", new SavingsAlgorithm(googleMapsService));
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

