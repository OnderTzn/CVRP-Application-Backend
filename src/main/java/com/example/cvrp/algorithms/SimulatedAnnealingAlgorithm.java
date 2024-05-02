package com.example.cvrp.algorithms;

import com.example.cvrp.dto.RouteLeg;
import com.example.cvrp.model.Address;
import com.example.cvrp.model.GoogleMapsResponse;
import com.example.cvrp.service.GoogleMapsServiceImp;

import java.util.*;

public class SimulatedAnnealingAlgorithm implements RoutingAlgorithm {

    private final GoogleMapsServiceImp googleMapsService;
    private double temperature;
    private double coolingRate;
    private final Random random = new Random();

    public SimulatedAnnealingAlgorithm(GoogleMapsServiceImp googleMapsService, double initialTemp, double coolingRate) {
        this.googleMapsService = googleMapsService;
        this.temperature = initialTemp;
        this.coolingRate = coolingRate;
    }

    @Override
    public List<RouteLeg> calculateRoute(List<Address> addresses, Long vehicleCapacity) {
        List<Address> currentRoute = createInitialRoute(addresses, vehicleCapacity);
        List<Address> bestRoute = new ArrayList<>(currentRoute);

        while (temperature > 1) {
            List<Address> newRoute = generateNeighbor(currentRoute, vehicleCapacity);
            double currentEnergy = calculateCost(currentRoute);
            double neighborEnergy = calculateCost(newRoute);

            if (acceptanceProbability(currentEnergy, neighborEnergy, temperature) > Math.random()) {
                currentRoute = new ArrayList<>(newRoute);
            }

            if (calculateCost(currentRoute) < calculateCost(bestRoute)) {
                bestRoute = new ArrayList<>(currentRoute);
            }

            temperature *= coolingRate;
        }

        return convertToRouteLegs(bestRoute, vehicleCapacity);
    }

    private Map<String, Double> distanceCache = new HashMap<>();

    private double calculateCost(List<Address> route) {
        double totalCost = 0.0;
        for (int i = 0; i < route.size() - 1; i++) {
            String key = createCacheKey(route.get(i), route.get(i + 1));
            if (!distanceCache.containsKey(key)) {
                GoogleMapsResponse response = googleMapsService.getDistanceMatrix(
                        String.format("%f,%f", route.get(i).getLatitude(), route.get(i).getLongitude()),
                        String.format("%f,%f", route.get(i + 1).getLatitude(), route.get(i + 1).getLongitude())
                );
                double distance = response.getRows().get(0).getElements().get(0).getDistance().getValue();
                distanceCache.put(key, distance);
            }
            totalCost += distanceCache.get(key);
        }
        return totalCost;
    }

    private String createCacheKey(Address origin, Address destination) {
        return origin.getId() + "-" + destination.getId();
    }


    private List<Address> generateNeighbor(List<Address> currentRoute, Long vehicleCapacity) {
        // Generate a neighbor route by swapping two addresses
        int a = random.nextInt(currentRoute.size());
        int b = random.nextInt(currentRoute.size());
        Collections.swap(currentRoute, a, b);
        return currentRoute;
    }

    private double acceptanceProbability(double energy, double newEnergy, double temperature) {
        if (newEnergy < energy) {
            return 1.0;
        }
        return Math.exp((energy - newEnergy) / temperature);
    }

    private List<RouteLeg> convertToRouteLegs(List<Address> addresses, Long vehicleCapacity) {
        List<RouteLeg> routeLegs = new ArrayList<>();
        double totalDistance = 0.0;
        double totalTime = 0.0;

        for (int i = 0; i < addresses.size() - 1; i++) {
            Address origin = addresses.get(i);
            Address destination = addresses.get(i + 1);
            double distance = getDistanceFromCache(origin, destination);
            double time = calculateTime(distance);  // Assuming you have a method to calculate time based on distance

            totalDistance += distance;
            totalTime += time;

            routeLegs.add(new RouteLeg(origin.getId(), destination.getId(), distance, time, vehicleCapacity));
        }

        System.out.println("Total Distance: " + totalDistance + " km");
        System.out.println("Total Time: " + totalTime + " hours");

        return routeLegs;
    }

    private double getDistanceFromCache(Address origin, Address destination) {
        String key = origin.getId() + "-" + destination.getId();
        if (!distanceCache.containsKey(key)) {
            double distance = googleMapsService.getDistanceMatrix(
                    String.format(Locale.US, "%f,%f", origin.getLatitude(), origin.getLongitude()),
                    String.format(Locale.US, "%f,%f", destination.getLatitude(), destination.getLongitude())
            ).getRows().get(0).getElements().get(0).getDistance().getValue();
            distanceCache.put(key, distance);
        }
        return distanceCache.get(key);
    }

    private double calculateTime(double distance) {
        double averageSpeed = 60.0; // Average speed in km/h
        return distance / averageSpeed; // Time in hours
    }


    private List<Address> createInitialRoute(List<Address> addresses, Long vehicleCapacity) {
        // Create an initial feasible route respecting vehicle capacities
        Collections.shuffle(addresses);
        return addresses; // This is a simplification
    }
}
