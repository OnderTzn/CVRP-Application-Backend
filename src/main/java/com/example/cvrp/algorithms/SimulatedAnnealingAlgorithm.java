package com.example.cvrp.algorithms;

import com.example.cvrp.dto.RouteLeg;
import com.example.cvrp.model.Address;
import com.example.cvrp.model.GoogleMapsResponse;
import com.example.cvrp.dto.TimeDistance;
import com.example.cvrp.service.GoogleMapsServiceImp;

import java.util.*;
import java.util.stream.Collectors;

public class SimulatedAnnealingAlgorithm implements RoutingAlgorithm {
    private final GoogleMapsServiceImp googleMapsService;
    private Map<String, TimeDistance> distanceCache = new HashMap<>();
    private final double initialTemperature = 10000;
    private double temperature = initialTemperature;
    private double coolingRate = 0.01;
    private int googleMapsRequestCount = 0;

    public SimulatedAnnealingAlgorithm(GoogleMapsServiceImp googleMapsService) {
        this.googleMapsService = googleMapsService;
    }

    @Override
    public List<RouteLeg> calculateRoute(Address depot, List<Address> addresses, long vehicleCapacity) {
        temperature = initialTemperature;
        int addressCount = addresses.size();
        double coolingRate;
        depot.setUnit(0L); // Ensure depot demand is 0

        // Determine the cooling rate based on the address count
        if (addressCount <= 16) {
            coolingRate = 0.01;
        } else if (addressCount <= 41) {
            coolingRate = 0.025;
        } else if (addressCount <= 101) {
            coolingRate = 0.025;
        } else {
            coolingRate = 0.025;
        }
        this.coolingRate = coolingRate;

        List<Address> currentSolution = generateInitialSolution(addresses, depot);
        List<Address> bestSolution = new ArrayList<>(currentSolution);

        while (temperature > 1) {
            List<Address> newSolution = generateNeighborSolution(currentSolution);

            double currentEnergy = calculateObjectiveValue(currentSolution, depot);
            double neighborEnergy = calculateObjectiveValue(newSolution, depot);

            if (acceptanceProbability(currentEnergy, neighborEnergy, temperature) > Math.random()) {
                currentSolution = new ArrayList<>(newSolution);
            }

            if (calculateObjectiveValue(currentSolution, depot) < calculateObjectiveValue(bestSolution, depot)) {
                bestSolution = new ArrayList<>(currentSolution);
                System.out.println("New best solution found: " + calculateObjectiveValue(bestSolution, depot));
            }

            temperature *= 1 - coolingRate;
        }

        List<RouteLeg> finalRouteLegs = convertToRouteLegs(bestSolution, depot, vehicleCapacity);


        System.out.println("Final Route:");
        for (RouteLeg leg : finalRouteLegs) {
            System.out.println("From ID: " + leg.getOriginId() + " To ID: " + leg.getDestinationId() +
                    " - Distance: " + leg.getDistance() + "m, Time: " + leg.getTime() + "s");
        }


        System.out.println("\nGoogle Maps API requests count in SA: " + googleMapsRequestCount);
        return finalRouteLegs;
    }

    // For testing purposes
    public List<RouteLeg> calculateRoute(List<Address> addresses, Long vehicleCapacity) {
        temperature = initialTemperature;
        System.out.println("Simulated Annealing Algorithm");
        int addressCount = addresses.size();
        double coolingRate;

        // Determine the cooling rate based on the address count
        if (addressCount <= 16) {
            coolingRate = 0.01;
        } else if (addressCount <= 41) {
            coolingRate = 0.025;
        } else if (addressCount <= 101) {
            coolingRate = 0.025;
        } else {
            coolingRate = 0.025;
        }
        this.coolingRate = coolingRate;

        Address depot = findDepot(addresses);
        depot.setUnit(0L); // Ensure depot demand is 0
        List<Address> addressesWithoutDepot = addresses.stream()
                .filter(address -> !address.equals(depot))
                .collect(Collectors.toList());
        List<Address> currentSolution = generateInitialSolution(addressesWithoutDepot, depot);
        List<Address> bestSolution = new ArrayList<>(currentSolution);

        System.out.println("Received addresses from SA:");
        for (Address address : addresses) {
            System.out.println("ID: " + address.getId() + ", Latitude: " + address.getLatitude() + ", Longitude: " + address.getLongitude() + "  Capacity: " + address.getUnit());
        }

        while (temperature > 1) {
            //System.out.println("TEMPERATURE: " + temperature);
            List<Address> newSolution = generateNeighborSolution(currentSolution);

            // Adjust the solution for capacity constraints
            //adjustForCapacity(newSolution, vehicleCapacity);

            double currentEnergy = calculateObjectiveValue(currentSolution, depot);
            double neighborEnergy = calculateObjectiveValue(newSolution, depot);

            if (acceptanceProbability(currentEnergy, neighborEnergy, temperature) > Math.random()) {
                currentSolution = new ArrayList<>(newSolution);
            }

            if (calculateObjectiveValue(currentSolution, depot) < calculateObjectiveValue(bestSolution, depot)) {
                bestSolution = new ArrayList<>(currentSolution);
                System.out.println("New best solution found: " + calculateObjectiveValue(bestSolution, depot));
            }

            temperature *= 1 - coolingRate;
        }

        List<RouteLeg> finalRouteLegs = convertToRouteLegs(bestSolution, depot, vehicleCapacity);


        System.out.println("Final Route:");
        for (RouteLeg leg : finalRouteLegs) {
            System.out.println("From ID: " + leg.getOriginId() + " To ID: " + leg.getDestinationId() +
                    " - Distance: " + leg.getDistance() + "m, Time: " + leg.getTime() + "s, Capacity Used: " + leg.getVehicleCapacity() + " units");
        }


        System.out.println("\n\nGoogle Maps API requests count in Simulated Annealing: " + googleMapsRequestCount);
        return finalRouteLegs;
    }

    public List<Address> generateInitialSolution(List<Address> addresses, Address depot) {

        // Create a list for the initial solution with the depot as the first address
        List<Address> initialSolution = new ArrayList<>();
        initialSolution.add(depot);

        // Add the rest of the addresses in a shuffled order
        List<Address> shuffledAddresses = new ArrayList<>(addresses); // Shuffles the list of addresses excluding the depot
        Collections.shuffle(shuffledAddresses);
        initialSolution.addAll(shuffledAddresses);

        return initialSolution;
    }

    private List<Address> generateNeighborSolution(List<Address> currentSolution) {
        List<Address> neighborSolution = new ArrayList<>(currentSolution);
        Random random = new Random();
        int strategy = random.nextInt(3); // Randomly choose a strategy (0: Swap, 1: Reversal, 2: Insertion)

        switch (strategy) {
            case 0: // Swap
                int index1 = 1 + random.nextInt(neighborSolution.size() - 1);
                int index2 = 1 + random.nextInt(neighborSolution.size() - 1);
                while (index1 == index2) {
                    index2 = 1 + random.nextInt(neighborSolution.size() - 1);
                }
                Collections.swap(neighborSolution, index1, index2);
                break;

            case 1: // Reversal
                int start = 1 + random.nextInt(neighborSolution.size() - 2);
                int end = start + random.nextInt(neighborSolution.size() - start);
                Collections.reverse(neighborSolution.subList(start, end + 1));
                break;

            case 2: // Insertion
                int removeIndex = 1 + random.nextInt(neighborSolution.size() - 1);
                Address address = neighborSolution.remove(removeIndex);
                int insertIndex = 1 + random.nextInt(neighborSolution.size() - 1);
                neighborSolution.add(insertIndex, address);
                break;
        }

        return neighborSolution;
    }

    private double calculateObjectiveValue(List<Address> solution, Address depot) {
        double totalTravelTime = 0.0;  // Total time in seconds

        for (int i = 0; i < solution.size() - 1; i++) {
            // Fetch time and distance between consecutive addresses
            TimeDistance timeDistance = getTimeDistanceBetweenAddresses(solution.get(i), solution.get(i + 1));
            totalTravelTime += timeDistance.getTime();     // Accumulate time in seconds
        }

        Address lastAddress = solution.get(solution.size() - 1);
        TimeDistance backToDepot = getTimeDistanceBetweenAddresses(lastAddress, depot);
        totalTravelTime += backToDepot.getTime();

        return totalTravelTime;
    }

    private List<RouteLeg> convertToRouteLegs(List<Address> bestSolution, Address depot, Long vehicleCapacity) {
        List<RouteLeg> routeLegs = new ArrayList<>();
        Long currentCapacity = vehicleCapacity;

        for (int i = 0; i < bestSolution.size() - 1; i++) {
            Address from = bestSolution.get(i);
            Address to = bestSolution.get(i + 1);
            Long remainingDemand = to.getUnit();  // Make a copy of the demand

            while (remainingDemand > 0) {
                if (remainingDemand > currentCapacity) {
                    // Deliver as much as possible with the current capacity
                    routeLegs.addAll(deliverUnits(from, to, currentCapacity));
                    remainingDemand -= currentCapacity;
                    currentCapacity = vehicleCapacity;
                    routeLegs.addAll(returnToDepotAndRefill(to, depot));
                    from = depot;
                } else {
                    // Deliver the remaining units
                    routeLegs.addAll(deliverUnits(from, to, remainingDemand));
                    currentCapacity -= remainingDemand;
                    remainingDemand = 0L;
                }
            }

            // If the current capacity is 0 after unloading, return to the depot to refill before proceeding
            if (currentCapacity == 0) {
                routeLegs.addAll(returnToDepotAndRefill(to, depot));
                currentCapacity = vehicleCapacity;

                // Add leg from depot to next address with the next address' demand
                if (i < bestSolution.size() - 2) { // Check to avoid out-of-bounds error
                    Address nextTo = bestSolution.get(i + 2);
                    routeLegs.addAll(addLegFromDepotToNextAddress(depot, nextTo, nextTo.getUnit()));
                    i++; // Skip the next address as it's already processed
                }
            }
        }

        // Ensure the last leg returns to the depot
        addFinalLegToDepot(bestSolution, depot, routeLegs);

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

    private List<RouteLeg> deliverUnits(Address from, Address to, Long units) {
        List<RouteLeg> routeLegs = new ArrayList<>();
        TimeDistance timeDistance = getTimeDistanceBetweenAddresses(from, to);
        routeLegs.add(new RouteLeg(from.getId(), to.getId(), from.getLatitude(), from.getLongitude(),
                to.getLatitude(), to.getLongitude(), timeDistance.getTime(), timeDistance.getDistance(), units));
        return routeLegs;
    }

    private List<RouteLeg> returnToDepotAndRefill(Address from, Address depot) {
        List<RouteLeg> routeLegs = new ArrayList<>();
        TimeDistance backToDepot = getTimeDistanceBetweenAddresses(from, depot);
        routeLegs.add(new RouteLeg(from.getId(), depot.getId(), from.getLatitude(), from.getLongitude(),
                depot.getLatitude(), depot.getLongitude(), backToDepot.getTime(), backToDepot.getDistance(), 0L));
        return routeLegs;
    }

    private List<RouteLeg> addLegFromDepotToNextAddress(Address depot, Address nextTo, Long units) {
        List<RouteLeg> routeLegs = new ArrayList<>();
        TimeDistance fromDepot = getTimeDistanceBetweenAddresses(depot, nextTo);
        routeLegs.add(new RouteLeg(depot.getId(), nextTo.getId(), depot.getLatitude(), depot.getLongitude(),
                nextTo.getLatitude(), nextTo.getLongitude(), fromDepot.getTime(), fromDepot.getDistance(), units));
        return routeLegs;
    }

    private void addFinalLegToDepot(List<Address> bestSolution, Address depot, List<RouteLeg> routeLegs) {
        Address lastAddress = bestSolution.get(bestSolution.size() - 1);
        if (!lastAddress.equals(depot)) {
            TimeDistance backToDepot = getTimeDistanceBetweenAddresses(lastAddress, depot);
            routeLegs.add(new RouteLeg(lastAddress.getId(), depot.getId(), lastAddress.getLatitude(), lastAddress.getLongitude(),
                    depot.getLatitude(), depot.getLongitude(), backToDepot.getTime(), backToDepot.getDistance(), 0L));
        }
    }

    public double getInitialTemperature() {
        return initialTemperature;
    }

    public double getCoolingRate() {
        return coolingRate;
    }

}
