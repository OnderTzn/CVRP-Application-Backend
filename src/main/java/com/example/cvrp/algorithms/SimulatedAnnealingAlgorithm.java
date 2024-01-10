package com.example.cvrp.algorithms;

import com.example.cvrp.dto.RouteLeg;
import com.example.cvrp.model.Address;
import com.example.cvrp.model.GoogleMapsResponse;
import com.example.cvrp.model.TimeDistance;
import com.example.cvrp.service.GoogleMapsServiceImp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SimulatedAnnealingAlgorithm implements RoutingAlgorithm {
    // Define MAX_ATTEMPTS as a constant
    private static final int MAX_ATTEMPTS = 150;


    private final GoogleMapsServiceImp googleMapsService;
    private double temperature = 100;
    private double coolingRate = 0.3;
    private boolean allAddressesVisited = false;

    public SimulatedAnnealingAlgorithm(GoogleMapsServiceImp googleMapsService) {
        this.googleMapsService = googleMapsService;
    }

    public List<RouteLeg> calculateRoute(List<Address> addresses, Long vehicleCapacity) {
        List<Address> currentSolution = generateInitialSolution(addresses);
        List<Address> bestSolution = new ArrayList<>(currentSolution);
        Address depot = findDepot(addresses);
        System.out.println("Received addresses from SA:");
        for (Address address : addresses) {
            System.out.println("ID: " + address.getId() + ", Latitude: " + address.getLatitude() + ", Longitude: " + address.getLongitude());
        }

        while (temperature > 1) {
            System.out.println("TEMPERATURE: " + temperature);
            List<Address> newSolution = generateNeighborSolution(currentSolution, vehicleCapacity);

            // Adjust the solution for capacity constraints
            refillIfNecessary(newSolution, vehicleCapacity);

            double currentEnergy = calculateObjectiveValue(currentSolution);
            double neighborEnergy = calculateObjectiveValue(newSolution);

            if (acceptanceProbability(currentEnergy, neighborEnergy, temperature) > Math.random()) {
                currentSolution = new ArrayList<>(newSolution);
            }

            if (calculateObjectiveValue(currentSolution) < calculateObjectiveValue(bestSolution)) {
                bestSolution = new ArrayList<>(currentSolution);
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

        return convertToRouteLegs(bestSolution, depot);
    }



    public List<Address> generateInitialSolution(List<Address> addresses) {
        // Assuming the first address is the depot
        Address depot = findDepot(addresses);

        // Create a list for the initial solution with the depot as the first address
        List<Address> initialSolution = new ArrayList<>();
        initialSolution.add(depot);

        // Add the rest of the addresses in a shuffled order
        List<Address> shuffledAddresses = new ArrayList<>(addresses.subList(1, addresses.size()));
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
        adjustForCapacity(neighborSolution, vehicleCapacity);

        return neighborSolution;
    }

    private double calculateObjectiveValue(List<Address> solution) {
        double totalTravelTime = 0.0;  // Total time in seconds
        double totalDistance = 0.0;    // Total distance in meters

        for (int i = 0; i < solution.size() - 1; i++) {
            // Fetch time and distance between consecutive addresses
            TimeDistance timeDistance = getTimeDistanceBetweenAddresses(solution.get(i), solution.get(i + 1));
            totalTravelTime += timeDistance.getTime();     // Accumulate time in seconds
            totalDistance += timeDistance.getDistance();   // Accumulate distance in meters
        }

        // Convert time to hours and distance to kilometers for a more balanced comparison
        double timeInHours = totalTravelTime / 3600.0;
        double distanceInKilometers = totalDistance / 1000.0;

        // Define weights or factors as per requirement
        double timeWeightFactor = 1.0;   // Adjust as needed
        double distanceWeightFactor = 0.5; // Adjust as needed

        // Objective value considering both time and distance
        return (timeInHours * timeWeightFactor) + (distanceInKilometers * distanceWeightFactor);
    }



    private List<Address> adjustRouteForCapacity(List<Address> currentSolution, Long vehicleCapacity) {
        List<Address> adjustedSolution = new ArrayList<>();
        Long currentCapacity = vehicleCapacity;

        for (Address address : currentSolution) {
            if (!canVisitNextAddress(address, currentCapacity)) {
                // Add depot to the route and reset capacity
                adjustedSolution.add(currentSolution.get(0)); // Add depot
                currentCapacity = vehicleCapacity;
            }
            adjustedSolution.add(address);
            currentCapacity -= address.getUnit();
        }
        return adjustedSolution;
    }

    private void adjustForCapacity(List<Address> solution, Long vehicleCapacity) {
        long currentCapacity = vehicleCapacity;
        for (int i = 1; i < solution.size(); i++) { // Start from 1 to skip the depot
            Address address = solution.get(i);
            if (currentCapacity < address.getUnit()) {
                // Insert the depot address to refill and reset the capacity
                Address depot = findDepot(solution);
                solution.add(i, depot);
                currentCapacity = vehicleCapacity;
            }
            else {
                // Deduct the unit from the current capacity
                currentCapacity -= address.getUnit();
            }
        }
    }


    private void refillIfNecessary(List<Address> solution, Long vehicleCapacity) {
        Long currentCapacity = vehicleCapacity;
        for (int i = 0; i < solution.size(); i++) {
            Address address = solution.get(i);
            if (currentCapacity < address.getUnit()) {
                // Insert the depot as the next address and reset capacity
                solution.add(i, findDepot(solution));
                currentCapacity = vehicleCapacity;
                i++; // Skip the newly added depot address in the next iteration
            } else {
                currentCapacity -= address.getUnit();
            }
        }
    }

    private List<RouteLeg> convertToRouteLegs(List<Address> bestSolution, Address depot) {
        List<RouteLeg> routeLegs = new ArrayList<>();

        for (int i = 0; i < bestSolution.size() - 1; i++) {
            Address from = bestSolution.get(i);
            Address to = bestSolution.get(i + 1);
            TimeDistance timeDistance = getTimeDistanceBetweenAddresses(from, to);

            routeLegs.add(new RouteLeg(from.getId(), to.getId(),
                    to.getLatitude(), to.getLongitude(),
                    timeDistance.getTime(), timeDistance.getDistance()));
        }
        // Add a final leg from the last address to the depot
        Address lastAddress = bestSolution.get(bestSolution.size() - 1);
        TimeDistance timeDistanceToDepot = getTimeDistanceBetweenAddresses(lastAddress, depot);
        routeLegs.add(new RouteLeg(lastAddress.getId(), depot.getId(),
                depot.getLatitude(), depot.getLongitude(),
                timeDistanceToDepot.getTime(), timeDistanceToDepot.getDistance()));

        // Print the routeLegs before returning
        System.out.println("Complete Route Legs:");
        for (RouteLeg leg : routeLegs) {
            System.out.println("Leg from ID: " + leg.getOriginId() + " to ID: " + leg.getDestinationId() +
                    ", Time: " + leg.getTime() + "s, Distance: " + leg.getDistance() + "m");
        }

        return routeLegs;
    }

    private TimeDistance getTimeDistanceBetweenAddresses(Address from, Address to) {
        GoogleMapsResponse response = googleMapsService.getDistanceMatrix(
                from.getLatitude() + "," + from.getLongitude(),
                to.getLatitude() + "," + to.getLongitude()
        );

        if (response == null || response.getRows().isEmpty() || response.getRows().get(0).getElements().isEmpty()) {
            return new TimeDistance(Double.MAX_VALUE, Double.MAX_VALUE); // Handle error scenario
        }

        Double time = response.getRows().get(0).getElements().get(0).getDuration().getValue();
        Double distance = response.getRows().get(0).getElements().get(0).getDistance().getValue();

        return new TimeDistance(time, distance);
    }



    private double acceptanceProbability(double currentEnergy, double newEnergy, double temperature) {
        if (newEnergy < currentEnergy) {
            return 1.0;
        }
        return Math.exp((currentEnergy - newEnergy) / temperature);
    }


    private List<Address> refillAtDepot(List<Address> solution, Long currentCapacity, Long vehicleCapacity, Address depot) {
        if (currentCapacity <= 0) {
            // Insert depot to refill
            solution.add(0, depot); // Add depot at the beginning
            currentCapacity = vehicleCapacity; // Reset capacity
        }
        return solution;
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


