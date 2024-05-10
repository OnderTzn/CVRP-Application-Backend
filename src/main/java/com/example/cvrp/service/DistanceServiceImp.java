package com.example.cvrp.service;

import com.example.cvrp.algorithms.*;
import com.example.cvrp.dto.RouteCalculationResult;
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
        routingAlgorithms.put("Savings", new SavingsAlgorithm(googleMapsService));
        routingAlgorithms.put("SimulatedAnnealing", new SimulatedAnnealingAlgorithm(googleMapsService));
        routingAlgorithms.put("NearestNeighborSA", new NearestNeighborSA(googleMapsService));
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

    public List<RouteLeg> calculateRoute(String algorithm, Address depot, List<Address> addressList, Long vehicleCapacity) {
        RoutingAlgorithm selectedAlgorithm = routingAlgorithms.get(algorithm);
        if (selectedAlgorithm != null) {
            return selectedAlgorithm.calculateRouteWithDepot(depot, addressList, vehicleCapacity);
        } else {
            throw new IllegalArgumentException("NearestNeighbor algorithm is not available.");
        }
    }


    public RouteCalculationResult calculateRouteAndGetDetails(String algorithmType, int addressLimit, Long vehicleCapacity) {
        RoutingAlgorithm selectedAlgorithm = routingAlgorithms.get(algorithmType);
        if (selectedAlgorithm == null) {
            throw new IllegalArgumentException("Unknown routing algorithm: " + algorithmType);
        }

        // Start measuring execution time
        long startTime = System.currentTimeMillis();

        // Fetch the addresses based on the provided limit
        List<Address> addresses = addressService.findAllAddresses(addressLimit + 1);

        // Execute the routing algorithm
        List<RouteLeg> route = selectedAlgorithm.calculateRoute(addresses, vehicleCapacity);

        // Measure execution time
        long executionTime = System.currentTimeMillis() - startTime; // execution time in milliseconds

        // Calculate the total distance, total time, and returns to depot
        double totalDistance = route.stream().mapToDouble(RouteLeg::getDistance).sum();
        double totalTime = route.stream().mapToDouble(RouteLeg::getTime).sum();
        int returnsToDepot = (int) route.stream()
                .filter(leg -> leg.getDestinationId().equals(1L)) // Checks if the destination is the depot
                .count() - 1; // Subtract 1 to exclude the final mandatory return

        // Return a new RouteCalculationResult with the calculated values
        return new RouteCalculationResult(route, executionTime, totalDistance, totalTime, returnsToDepot);
    }


}

