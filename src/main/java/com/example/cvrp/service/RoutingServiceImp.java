package com.example.cvrp.service;

import com.example.cvrp.algorithms.*;
import com.example.cvrp.dto.RouteCalculationResult;
import com.example.cvrp.dto.RouteLeg;
import com.example.cvrp.model.Address;
import com.example.cvrp.model.AlgorithmResult;
import com.example.cvrp.model.RouteLegEntity;
import com.example.cvrp.util.MemoryUsageUtil;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RoutingServiceImp {
    private final GoogleMapsServiceImp googleMapsService;
    private final AddressService addressService;
    private final DistanceMatrixServiceImp distanceMatrixService;
    private final AlgorithmResultServiceImp algorithmResultServiceImp;
    private final Map<String, RoutingAlgorithm> routingAlgorithms;
    private final Map<String, RoutingAlgorithm> testRoutingAlgorithms;

    public RoutingServiceImp(GoogleMapsServiceImp googleMapsService, AddressService addressService, DistanceMatrixServiceImp distanceMatrixService, AlgorithmResultServiceImp algorithmResultServiceImp) {
        this.googleMapsService = googleMapsService;
        this.addressService = addressService;
        this.distanceMatrixService = distanceMatrixService;
        this.algorithmResultServiceImp = algorithmResultServiceImp;

        routingAlgorithms = new HashMap<>();
        routingAlgorithms.put("NearestNeighbor", new NearestNeighborAlgorithm(googleMapsService));
        routingAlgorithms.put("Savings", new SavingsAlgorithm(googleMapsService));
        routingAlgorithms.put("SimulatedAnnealing", new SimulatedAnnealingAlgorithm(googleMapsService));
        routingAlgorithms.put("NearestNeighborSA", new NearestNeighborSA(googleMapsService));

        // Initialize the test routing algorithms map
        testRoutingAlgorithms = new HashMap<>();
        testRoutingAlgorithms.put("NearestNeighborTest", new NearestNeighborAlgorithmTest(distanceMatrixService));
        testRoutingAlgorithms.put("SavingsTest", new SavingsAlgorithmTest(distanceMatrixService));
        testRoutingAlgorithms.put("SimulatedAnnealingTest", new SimulatedAnnealingAlgorithmTest(distanceMatrixService));
        testRoutingAlgorithms.put("NearestNeighborSATest", new NearestNeighborSATest(distanceMatrixService));

        testRoutingAlgorithms.put("NearestNeighbor", new NearestNeighborAlgorithm(googleMapsService));
        testRoutingAlgorithms.put("Savings", new SavingsAlgorithm(googleMapsService));
        testRoutingAlgorithms.put("SimulatedAnnealing", new SimulatedAnnealingAlgorithm(googleMapsService));
        testRoutingAlgorithms.put("NearestNeighborSA", new NearestNeighborSA(googleMapsService));
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
            List<RouteLeg> route = selectedAlgorithm.calculateRoute(depot, addressList, vehicleCapacity);

            // Calculate the total distance, total time, and returns to depot
            double totalDistance = route.stream().mapToDouble(RouteLeg::getDistance).sum();
            double totalTime = route.stream().mapToDouble(RouteLeg::getTime).sum();
            int returnsToDepot = (int) route.stream()
                    .filter(leg -> leg.getDestinationId().equals(1L)) // Checks if the destination is the depot
                    .count() - 1; // Subtract 1 to exclude the final mandatory return

            // Save the results
            Double initialTemperature = null;
            Double coolingRate = null;
            if (selectedAlgorithm instanceof SimulatedAnnealingAlgorithm) {
                initialTemperature = ((SimulatedAnnealingAlgorithm) selectedAlgorithm).getInitialTemperature();
                coolingRate = ((SimulatedAnnealingAlgorithm) selectedAlgorithm).getCoolingRate();
            } else if (selectedAlgorithm instanceof NearestNeighborSA) {
                initialTemperature = ((NearestNeighborSA) selectedAlgorithm).getInitialTemperature();
                coolingRate = ((NearestNeighborSA) selectedAlgorithm).getCoolingRate();
            }

            AlgorithmResult result = new AlgorithmResult(
                    algorithm, addressList.size(), vehicleCapacity, initialTemperature, coolingRate,
                    totalTime, totalDistance, 0, 0, returnsToDepot
            );

            // Convert RouteLeg DTOs to RouteLegEntities
            List<RouteLegEntity> routeLegEntities = route.stream()
                    .map(leg -> new RouteLegEntity(
                            leg.getOriginId(), leg.getDestinationId(), leg.getLatitude(),
                            leg.getLongitude(), leg.getDestLatitude(), leg.getDestLongitude(),
                            leg.getTime(), leg.getDistance(), leg.getVehicleCapacity()
                    ))
                    .collect(Collectors.toList());

            algorithmResultServiceImp.saveResult(result, routeLegEntities); // For saving the results and route legs to the database

            return route;
        } else {
            throw new IllegalArgumentException("Unknown routing algorithm: " + algorithm);
        }
    }


    public RouteCalculationResult calculateRouteTest(String algorithmType, int addressLimit, Long vehicleCapacity) {
        RoutingAlgorithm selectedAlgorithm = testRoutingAlgorithms.get(algorithmType);
        if (selectedAlgorithm == null) {
            throw new IllegalArgumentException("Unknown routing algorithm: " + algorithmType);
        }
        MemoryUsageUtil.forceGarbageCollection();
        long memoryBefore = MemoryUsageUtil.getUsedMemory();

        // Start measuring execution time
        long startTime = System.currentTimeMillis();

        // Fetch the addresses based on the provided limit
        List<Address> addresses = addressService.findAllAddresses(addressLimit + 1);

        // Execute the routing algorithm
        List<RouteLeg> route = selectedAlgorithm.calculateRoute(addresses, vehicleCapacity);

        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime; // execution time in milliseconds

        // Calculate the total distance, total time, and returns to depot
        double totalDistance = route.stream().mapToDouble(RouteLeg::getDistance).sum();
        double totalTime = route.stream().mapToDouble(RouteLeg::getTime).sum();
        int returnsToDepot = (int) route.stream()
                .filter(leg -> leg.getDestinationId().equals(1L)) // Checks if the destination is the depot
                .count() - 1; // Subtract 1 to exclude the final mandatory return

        int googleMapsRequestCount = distanceMatrixService.getGoogleMapsRequestCount();
        System.out.println("Number of Google Maps API requests in test classes: " + googleMapsRequestCount);

        MemoryUsageUtil.forceGarbageCollection();
        long memoryAfter = MemoryUsageUtil.getUsedMemory();
        long memoryUsed = memoryAfter - memoryBefore;

        // Save the results
        Double initialTemperature = null;
        Double coolingRate = null;
        if (selectedAlgorithm instanceof SimulatedAnnealingAlgorithmTest) {
            initialTemperature = ((SimulatedAnnealingAlgorithmTest) selectedAlgorithm).getInitialTemperature();
            coolingRate = ((SimulatedAnnealingAlgorithmTest) selectedAlgorithm).getCoolingRate();
        }
        else if (selectedAlgorithm instanceof NearestNeighborSATest) {
            initialTemperature = ((NearestNeighborSATest) selectedAlgorithm).getInitialTemperature();
            coolingRate = ((NearestNeighborSATest) selectedAlgorithm).getCoolingRate();
        }

        // Remove "Test" suffix from algorithmType if it exists
        String cleanedAlgorithmType = algorithmType.replaceAll("Test$", "");

        // Save the results
        AlgorithmResult result = new AlgorithmResult(
                cleanedAlgorithmType, addressLimit, vehicleCapacity, initialTemperature, coolingRate,
                totalTime, totalDistance, executionTime, memoryUsed, returnsToDepot
        );

        // Convert RouteLeg DTOs to RouteLegEntities
        List<RouteLegEntity> routeLegEntities = route.stream()
                .map(leg -> new RouteLegEntity(
                        leg.getOriginId(), leg.getDestinationId(), leg.getLatitude(),
                        leg.getLongitude(), leg.getDestLatitude(), leg.getDestLongitude(),
                        leg.getTime(), leg.getDistance(), leg.getVehicleCapacity()
                ))
                .collect(Collectors.toList());

        //algorithmResultService.saveResult(result); // For saving the results to the database
        algorithmResultServiceImp.saveResult(result, routeLegEntities); // For saving the results and route legs to the database

        // Prompt the user for input
        /*Scanner scanner = new Scanner(System.in);
        System.out.println("Save results? Enter 1 to save result, 2 to save result and route legs, any other key to skip:");
        int input = scanner.nextInt();

        // Save based on user input
        if (input == 1) {
            algorithmResultService.saveResult(result); // For saving the results to the database
        } else if (input == 2) {
            algorithmResultService.saveResult(result, routeLegEntities); // For saving the results and route legs to the database
        }*/

        // Return a new RouteCalculationResult with the calculated values
        return new RouteCalculationResult(route, executionTime, totalDistance, totalTime, returnsToDepot);
    }
}
