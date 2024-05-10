package com.example.cvrp.algorithms;

import com.example.cvrp.dto.RouteLeg;
import com.example.cvrp.model.Address;
import com.example.cvrp.model.GoogleMapsResponse;
import com.example.cvrp.model.TimeDistance;
import com.example.cvrp.service.GoogleMapsServiceImp;

import java.util.*;
import java.util.stream.Collectors;

public class SimulatedAnnealingAlgorithm implements RoutingAlgorithm {
    // Define MAX_ATTEMPTS as a constant
    //private static final int MAX_ATTEMPTS = 150;


    private final GoogleMapsServiceImp googleMapsService;
    private Map<String, TimeDistance> distanceCache = new HashMap<>();
    private double temperature = 10000;
    private double coolingRate = 0.0001;
    private boolean allAddressesVisited = false;
    private int googleMapsRequestCount = 0;

    public SimulatedAnnealingAlgorithm(GoogleMapsServiceImp googleMapsService) {
        this.googleMapsService = googleMapsService;
    }

    @Override
    public List<RouteLeg> calculateRouteWithDepot(Address depot, List<Address> addresses, long vehicleCapacity) {
        List<Address> currentSolution = generateInitialSolution(addresses, depot);
        List<Address> bestSolution = new ArrayList<>(currentSolution);

        System.out.println("Received addresses from SA:");
        for (Address address : addresses) {
            System.out.println("ID: " + address.getId() + ", Latitude: " + address.getLatitude() + ", Longitude: " + address.getLongitude());
        }

        while (temperature > 1) {
            System.out.println("TEMPERATURE: " + temperature);
            List<Address> newSolution = generateNeighborSolution(currentSolution, vehicleCapacity);

            // Adjust the solution for capacity constraints
            //adjustForCapacity(newSolution, vehicleCapacity);

            double currentEnergy = calculateObjectiveValue(currentSolution);
            double neighborEnergy = calculateObjectiveValue(newSolution);

            if (acceptanceProbability(currentEnergy, neighborEnergy, temperature) > Math.random()) {
                currentSolution = new ArrayList<>(newSolution);
            }

            if (calculateObjectiveValue(currentSolution) < calculateObjectiveValue(bestSolution)) {
                bestSolution = new ArrayList<>(currentSolution);
                System.out.println("New best solution found: " + calculateObjectiveValue(bestSolution));
            }

            // Check if all addresses are included in the current solution
            if (!checkAllAddressesVisited(currentSolution, addresses)) {
                // Consider additional logic or iterations to cover unvisited addresses
            }
            else {
                allAddressesVisited = true;
            }

            temperature *= 1 - coolingRate;
        }

        List<RouteLeg> finalRouteLegs = convertToRouteLegs(bestSolution, depot, vehicleCapacity);


        System.out.println("Final Route:");
        for (RouteLeg leg : finalRouteLegs) {
            System.out.println("From ID: " + leg.getOriginId() + " To ID: " + leg.getDestinationId() +
                    " - Distance: " + leg.getDistance() + "m, Time: " + leg.getTime() + "s");
        }


        System.out.println("\n\nGoogle Maps API requests count: " + googleMapsRequestCount);
        return finalRouteLegs;
    }

    public List<RouteLeg> calculateRoute(List<Address> addresses, Long vehicleCapacity) {

        Address depot = findDepot(addresses);
        List<Address> currentSolution = generateInitialSolution(addresses, depot);
        List<Address> bestSolution = new ArrayList<>(currentSolution);

        System.out.println("Received addresses from SA:");
        for (Address address : addresses) {
            System.out.println("ID: " + address.getId() + ", Latitude: " + address.getLatitude() + ", Longitude: " + address.getLongitude());
        }

        while (temperature > 1) {
            System.out.println("TEMPERATURE: " + temperature);
            List<Address> newSolution = generateNeighborSolution(currentSolution, vehicleCapacity);

            // Adjust the solution for capacity constraints
            //adjustForCapacity(newSolution, vehicleCapacity);

            double currentEnergy = calculateObjectiveValue(currentSolution);
            double neighborEnergy = calculateObjectiveValue(newSolution);

            if (acceptanceProbability(currentEnergy, neighborEnergy, temperature) > Math.random()) {
                currentSolution = new ArrayList<>(newSolution);
            }

            if (calculateObjectiveValue(currentSolution) < calculateObjectiveValue(bestSolution)) {
                bestSolution = new ArrayList<>(currentSolution);
                System.out.println("New best solution found: " + calculateObjectiveValue(bestSolution));
            }

            // Check if all addresses are included in the current solution
            if (!checkAllAddressesVisited(currentSolution, addresses)) {
                // Consider additional logic or iterations to cover unvisited addresses
            }
            else {
                allAddressesVisited = true;
            }

            temperature *= 1 - coolingRate;
        }

        List<RouteLeg> finalRouteLegs = convertToRouteLegs(bestSolution, depot, vehicleCapacity);


        System.out.println("Final Route:");
        for (RouteLeg leg : finalRouteLegs) {
            System.out.println("From ID: " + leg.getOriginId() + " To ID: " + leg.getDestinationId() +
                    " - Distance: " + leg.getDistance() + "m, Time: " + leg.getTime() + "s");
        }


        System.out.println("\n\nGoogle Maps API requests count: " + googleMapsRequestCount);
        return finalRouteLegs;
    }



    public List<Address> generateInitialSolution(List<Address> addresses, Address depot) {

        // Create a list for the initial solution with the depot as the first address
        List<Address> initialSolution = new ArrayList<>();
        initialSolution.add(depot);

        // Add the rest of the addresses in a shuffled order
        List<Address> shuffledAddresses = new ArrayList<>(addresses.subList(1, addresses.size())); // Shuffles the list of addresses excluding the depot
        Collections.shuffle(shuffledAddresses);
        initialSolution.addAll(shuffledAddresses);

        return initialSolution;
    }

    private List<Address> generateNeighborSolution(List<Address> currentSolution, Long vehicleCapacity) {
        List<Address> neighborSolution = new ArrayList<>(currentSolution);

        // Randomly select two indices to swap, ensuring that depot (index 0) is not selected
        int index1 = 1 + (int) (Math.random() * (neighborSolution.size() - 1));
        int index2 = 1 + (int) (Math.random() * (neighborSolution.size() - 1));

        // Ensure two different indices are selected
        while (index1 == index2) {
            index2 = 1 + (int) (Math.random() * (neighborSolution.size() - 1));
        }

        // Swap the addresses at these indices
        Collections.swap(neighborSolution, index1, index2);

        // Adjust for capacity if necessary
        //adjustForCapacity(neighborSolution, vehicleCapacity);

        return neighborSolution;
    }

    private double calculateObjectiveValue(List<Address> solution) {
        double totalTravelTime = 0.0;  // Total time in seconds
        double totalDistance = 0.0;    // Total distance in meters

        for (int i = 0; i < solution.size() - 1; i++) {
            // Fetch time and distance between consecutive addresses
            TimeDistance timeDistance = getTimeDistanceBetweenAddresses(solution.get(i), solution.get(i + 1));
            totalTravelTime += timeDistance.getTime();     // Accumulate time in seconds
            //totalDistance += timeDistance.getDistance();   // Accumulate distance in meters
        }

        return totalTravelTime;

    }

    private void adjustForCapacity(List<Address> solution, Long vehicleCapacity) {
        long currentCapacity = vehicleCapacity;
        Address depot = findDepot(solution); // Find the depot once

        for (int i = 1; i < solution.size(); i++) { // Skip depot at index 0
            Address address = solution.get(i);
            if (currentCapacity < address.getUnit()) {
                // Insert the depot address to refill and reset the capacity
                solution.add(i, depot);
                currentCapacity = vehicleCapacity;
            } else {
                currentCapacity -= address.getUnit();
            }
        }
    }

    private List<RouteLeg> convertToRouteLegs(List<Address> bestSolution, Address depot, Long vehicleCapacity) {
        List<RouteLeg> routeLegs = new ArrayList<>();
        Long currentCapacity = vehicleCapacity;

        for (int i = 0; i < bestSolution.size() - 1; i++) {
            Address from = bestSolution.get(i);
            Address to = bestSolution.get(i + 1);

            // Check if the next address exceeds the current capacity
            if (to.getUnit() > currentCapacity) {
                // Add leg back to depot from 'from'
                TimeDistance backToDepot = getTimeDistanceBetweenAddresses(from, depot);
                routeLegs.add(new RouteLeg(from.getId(), depot.getId(), from.getLatitude(), from.getLongitude(), depot.getLatitude(), depot.getLongitude(), backToDepot.getTime(), backToDepot.getDistance()));
                // Reset capacity
                currentCapacity = vehicleCapacity;
                // Add leg from depot to 'to'
                TimeDistance fromDepot = getTimeDistanceBetweenAddresses(depot, to);
                routeLegs.add(new RouteLeg(depot.getId(), to.getId(), depot.getLatitude(), depot.getLongitude(), to.getLatitude(), to.getLongitude(), fromDepot.getTime(), fromDepot.getDistance()));
            } else {
                // Add leg from 'from' to 'to'
                TimeDistance timeDistance = getTimeDistanceBetweenAddresses(from, to);
                routeLegs.add(new RouteLeg(from.getId(), to.getId(), from.getLatitude(), from.getLongitude(), to.getLatitude(), to.getLongitude(), timeDistance.getTime(), timeDistance.getDistance()));
            }
            currentCapacity -= to.getUnit();
        }

        // Ensure the last leg returns to the depot if not already there
        Address lastAddress = bestSolution.get(bestSolution.size() - 1);
        if (!lastAddress.equals(depot)) {
            TimeDistance backToDepot = getTimeDistanceBetweenAddresses(lastAddress, depot);
            routeLegs.add(new RouteLeg(lastAddress.getId(), depot.getId(), lastAddress.getLatitude(), lastAddress.getLongitude(), depot.getLatitude(), depot.getLongitude(), backToDepot.getTime(), backToDepot.getDistance()));
        }

        return routeLegs;
    }

    private TimeDistance getTimeDistanceBetweenAddresses(Address from, Address to) {

        String cacheKey = from.getLatitude() + "," + from.getLongitude() + "->" + to.getLatitude() + "," + to.getLongitude();

        if (distanceCache.containsKey(cacheKey)) {
            return distanceCache.get(cacheKey);
        }

        GoogleMapsResponse response = googleMapsService.getDistanceMatrix(
                from.getLatitude() + "," + from.getLongitude(),
                to.getLatitude() + "," + to.getLongitude()
        );
        googleMapsRequestCount++;

        if (response == null || response.getRows().isEmpty() || response.getRows().get(0).getElements().isEmpty()) {
            TimeDistance errorResult = new TimeDistance(Double.MAX_VALUE, Double.MAX_VALUE); // Handle error
            distanceCache.put(cacheKey, errorResult);
            return errorResult;
        }

        Double time = response.getRows().get(0).getElements().get(0).getDuration().getValue();
        Double distance = response.getRows().get(0).getElements().get(0).getDistance().getValue();
        TimeDistance result = new TimeDistance(time, distance);

        // Cache the result
        distanceCache.put(cacheKey, result);

        return result;
    }


    private double acceptanceProbability(double currentEnergy, double newEnergy, double temperature) {
        if (newEnergy < currentEnergy) {
            return 1.0;
        }
        return Math.exp((currentEnergy - newEnergy) / temperature);
    }

    private Address findDepot(List<Address> addresses) {
        return addresses.stream()
                .filter(address -> address.getId().equals(1L)) // Assuming depot's ID is 1
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Depot not found"));
    }


    private boolean checkAllAddressesVisited(List<Address> currentSolution, List<Address> allAddresses) {
        Set<Long> visitedAddressIds = currentSolution.stream().map(Address::getId).collect(Collectors.toSet());
        return allAddresses.stream().allMatch(address -> visitedAddressIds.contains(address.getId()));
    }



    private boolean canVisitNextAddress(Address nextAddress, Long currentCapacity) {
        return nextAddress.getUnit() <= currentCapacity;
    }

}
