package com.example.cvrp.algorithms;

import com.example.cvrp.dto.RouteLeg;
import com.example.cvrp.model.Address;
import com.example.cvrp.model.GoogleMapsResponse;
import com.example.cvrp.model.TimeDistance;
import com.example.cvrp.service.GoogleMapsServiceImp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SimulatedAnnealingAlgorithm implements RoutingAlgorithm {

    private final GoogleMapsServiceImp googleMapsService;
    private double temperature = 10000;
    private double coolingRate = 0.003;

    public SimulatedAnnealingAlgorithm(GoogleMapsServiceImp googleMapsService) {
        this.googleMapsService = googleMapsService;
    }

    public List<RouteLeg> calculateRoute(List<Address> addresses, Long vehicleCapacity) {
        // Initial solution
        List<Address> currentSolution = generateInitialSolution(addresses);

        // Best solution found
        List<Address> bestSolution = new ArrayList<>(currentSolution);

        while (temperature > 1) {
            // Generate a neighbor solution
            List<Address> newSolution = generateNeighborSolution(currentSolution);

            // Calculate the objective values of the solutions
            double currentEnergy = calculateObjectiveValue(currentSolution);
            double neighborEnergy = calculateObjectiveValue(newSolution);

            // Decide if we should accept the neighbor
            if (acceptanceProbability(currentEnergy, neighborEnergy, temperature) > Math.random()) {
                currentSolution = new ArrayList<>(newSolution);
            }

            // Update the best solution found
            if (calculateObjectiveValue(currentSolution) < calculateObjectiveValue(bestSolution)) {
                bestSolution = new ArrayList<>(currentSolution);
            }

            // Cool the system
            temperature *= 1 - coolingRate;
        }

        // Convert the best solution found to RouteLegs
        return convertToRouteLegs(bestSolution);
    }


    private List<Address> generateInitialSolution(List<Address> addresses) {
        // For simplicity, start with a sequential approach
        List<Address> initialSolution = new ArrayList<>(addresses);
        return initialSolution;
    }

    private List<Address> generateNeighborSolution(List<Address> currentSolution) {
        // Create a copy of the current solution
        List<Address> neighborSolution = new ArrayList<>(currentSolution);

        // Randomly select two indices to swap
        int index1 = (int) (Math.random() * neighborSolution.size());
        int index2 = (int) (Math.random() * neighborSolution.size());

        // Ensure two different indices are selected
        while(index1 == index2) {
            index2 = (int) (Math.random() * neighborSolution.size());
        }

        // Swap the addresses at these indices
        Collections.swap(neighborSolution, index1, index2);

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


    private double acceptanceProbability(double currentEnergy, double newEnergy, double temperature) {
        // If the new solution is better, accept it
        if (newEnergy < currentEnergy) {
            return 1.0;
        }
        // If the new solution is worse, calculate an acceptance probability
        return Math.exp((currentEnergy - newEnergy) / temperature);
    }

    private List<RouteLeg> convertToRouteLegs(List<Address> solution) {
        List<RouteLeg> routeLegs = new ArrayList<>();
        for (int i = 0; i < solution.size() - 1; i++) {
            Address from = solution.get(i);
            Address to = solution.get(i + 1);
            TimeDistance timeDistance = getTimeDistanceBetweenAddresses(from, to);

            routeLegs.add(new RouteLeg(from.getId(), to.getId(),
                    to.getLatitude(), to.getLongitude(),
                    timeDistance.getTime(), timeDistance.getDistance()));
        }
        return routeLegs;
    }

    private TimeDistance getTimeDistanceBetweenAddresses(Address origin, Address destination) {
        // Call the Google Maps API via the GoogleMapsServiceImp
        GoogleMapsResponse response = googleMapsService.getDistanceMatrix(
                origin.getLatitude() + "," + origin.getLongitude(),
                destination.getLatitude() + "," + destination.getLongitude());

        if (response == null || response.getRows() == null || response.getRows().isEmpty() ||
                response.getRows().get(0).getElements() == null || response.getRows().get(0).getElements().isEmpty()) {
            // Handle the scenario where no response is received
            System.out.println("No response received from Google Maps for addresses: " + origin + " and " + destination);
            return new TimeDistance(Double.MAX_VALUE, Double.MAX_VALUE); // Return a very high value to indicate an invalid route
        }

        // Extract time and distance from the response
        Double time = response.getRows().get(0).getElements().get(0).getDuration().getValue();
        Double distance = response.getRows().get(0).getElements().get(0).getDistance().getValue();

        return new TimeDistance(time, distance);
    }


}


