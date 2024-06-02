package com.example.cvrp.algorithms;

import com.example.cvrp.dto.RouteLeg;
import com.example.cvrp.model.Address;
import com.example.cvrp.model.GoogleMapsResponse;
import com.example.cvrp.dto.TimeDistance;
import com.example.cvrp.service.GoogleMapsServiceImp;

import java.util.*;

public class NearestNeighborSA implements RoutingAlgorithm {

    private final GoogleMapsServiceImp googleMapsService;
    private Map<String, TimeDistance> distanceCache = new HashMap<>();
    private final double initialTemperature = 10000;
    private double temperature = initialTemperature;
    private double coolingRate = 0.01;
    private int googleMapsRequestCount = 0;


    public NearestNeighborSA(GoogleMapsServiceImp googleMapsService) {
        this.googleMapsService = googleMapsService;
    }

    @Override
    public List<RouteLeg> calculateRoute(Address depot, List<Address> addresses, long vehicleCapacity) {
        // Ensure the depot is included in the addresses list if not already
        if (!addresses.contains(depot)) {
            addresses.add(0, depot); // Add depot as the first address if it's not included
        }
        temperature = initialTemperature;
        int addressCount = addresses.size();
        double coolingRate;

        if (addressCount <= 16) {
            coolingRate = 0.01;
        } else if (addressCount <= 41) {
            coolingRate = 0.025;
        } else if (addressCount <= 101) {
            coolingRate = 0.025;
        } else {
            coolingRate = 0.025;
        }
        depot.setUnit(0L); // Ensure depot demand is 0

        // Use Nearest Neighbor to get the initial route as addresses
        List<Address> currentSolution = generateInitialSolutionForSA(depot, addresses);

        List<Address> bestSolution = new ArrayList<>(currentSolution);

        while (temperature > 1) {
            //System.out.println("TEMPERATURE: " + temperature);
            List<Address> newSolution = generateNeighborSolution(currentSolution, vehicleCapacity);

            double currentEnergy = calculateObjectiveValue(currentSolution);
            double neighborEnergy = calculateObjectiveValue(newSolution);

            if (acceptanceProbability(currentEnergy, neighborEnergy, temperature) > Math.random()) {
                currentSolution = new ArrayList<>(newSolution);
            }

            if (calculateObjectiveValue(currentSolution) < calculateObjectiveValue(bestSolution)) {
                bestSolution = new ArrayList<>(currentSolution);
                System.out.println("New best solution found: " + calculateObjectiveValue(bestSolution));
            }

            temperature *= 1 - coolingRate;
        }

        List<RouteLeg> finalRouteLegs = convertToRouteLegs(bestSolution, depot, vehicleCapacity);


        System.out.println("Final Route:");
        for (RouteLeg leg : finalRouteLegs) {
            System.out.println("From ID: " + leg.getOriginId() + " To ID: " + leg.getDestinationId() +
                    " - Distance: " + leg.getDistance() + "m, Time: " + leg.getTime() + "s");
        }


        System.out.println("\n\nGoogle Maps API requests count in Nearest Neighbor SA: " + googleMapsRequestCount);
        return finalRouteLegs;
    }

    @Override
    public List<RouteLeg> calculateRoute(List<Address> addresses, Long vehicleCapacity) {
        System.out.println("Nearest Neighbor SA Algorithm");
        temperature = initialTemperature;
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
        List<Address> currentSolution = generateInitialSolutionForSA(addresses);
        List<Address> bestSolution = new ArrayList<>(currentSolution);

        /*System.out.println("Received addresses from SA:");
        for (Address address : addresses) {
            System.out.println("ID: " + address.getId() + ", Latitude: " + address.getLatitude() + ", Longitude: " + address.getLongitude());
        }*/

        while (temperature > 1) {

            //System.out.println("TEMPERATURE: " + temperature);

            List<Address> newSolution = generateNeighborSolution(currentSolution, vehicleCapacity);

            double currentEnergy = calculateObjectiveValue(currentSolution);
            double neighborEnergy = calculateObjectiveValue(newSolution);

            if (acceptanceProbability(currentEnergy, neighborEnergy, temperature) > Math.random()) {
                currentSolution = new ArrayList<>(newSolution);
            }

            if (calculateObjectiveValue(currentSolution) < calculateObjectiveValue(bestSolution)) {
                bestSolution = new ArrayList<>(currentSolution);
                System.out.println("New best solution found: " + calculateObjectiveValue(bestSolution));
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

    // Nearest Neighbor part
    // Nearest Neighbor part
    // Nearest Neighbor part

    public List<Address> generateInitialSolutionForSA(Address depot, List<Address> addresses) {
        List<Address> routeAddresses = new ArrayList<>();
        if (addresses == null || addresses.isEmpty()) {
            return routeAddresses; // Return an empty list if input is null or empty
        }

        // Ensure the depot is the starting point of the route
        routeAddresses.add(depot);

        List<Address> tempAddresses = new ArrayList<>(addresses);
        tempAddresses.remove(depot); // Remove depot from the list to avoid re-visiting

        Address currentAddress = depot;

        while (!tempAddresses.isEmpty()) {
            Address nextAddress = findNearestDestinationWithoutCapacity(currentAddress, tempAddresses);
            if (nextAddress == null) {
                // If no feasible next address found, it might indicate a logic error since capacity is not considered
                throw new IllegalStateException("No feasible next address found without considering capacity.");
            }

            // Add the next address to the route and update the current address
            routeAddresses.add(nextAddress);
            tempAddresses.remove(nextAddress);
            currentAddress = nextAddress;
        }

        return routeAddresses;
    }

    public List<Address> generateInitialSolutionForSA(List<Address> addresses) {
        List<Address> routeAddresses = new ArrayList<>();
        Address depot = addresses.get(0);

        if (addresses == null || addresses.isEmpty()) {
            return routeAddresses; // Return an empty list if input is null or empty
        }

        // Ensure the depot is the starting point of the route
        routeAddresses.add(depot);

        List<Address> tempAddresses = new ArrayList<>(addresses);
        tempAddresses.remove(depot); // Remove depot from the list to avoid re-visiting

        Address currentAddress = depot;

        while (!tempAddresses.isEmpty()) {
            Address nextAddress = findNearestDestinationWithoutCapacity(currentAddress, tempAddresses);
            if (nextAddress == null) {
                // If no feasible next address found, it might indicate a logic error since capacity is not considered
                throw new IllegalStateException("No feasible next address found without considering capacity.");
            }

            // Add the next address to the route and update the current address
            routeAddresses.add(nextAddress);
            tempAddresses.remove(nextAddress);
            currentAddress = nextAddress;
        }

        // Print the routeAddresses before returning
        System.out.println("Generated Initial Solution for SA:");
        for (Address address : routeAddresses) {
            System.out.println("Address ID: " + address.getId() + ", Latitude: " + address.getLatitude() + ", Longitude: " + address.getLongitude());
        }

        return routeAddresses;
    }

    private Address findNearestDestinationWithoutCapacity(Address origin, List<Address> potentialDestinations) {
        Address optimalDestination = null;
        Double shortestTime = Double.MAX_VALUE;
        Double shortestDistance = Double.MAX_VALUE;

        for (Address destination : potentialDestinations) {
            if (!destination.equals(origin)) {
                GoogleMapsResponse response = getGoogleMapsResponse(origin, destination);

                if (response != null && response.getRows() != null && !response.getRows().isEmpty()) {
                    Double time = response.getRows().get(0).getElements().get(0).getDuration().getValue();
                    Double distance = response.getRows().get(0).getElements().get(0).getDistance().getValue();

                    if (time < shortestTime || (time.equals(shortestTime) && distance < shortestDistance)) {
                        shortestTime = time;
                        shortestDistance = distance;
                        optimalDestination = destination;
                    }
                }
            }
        }
        return optimalDestination;
    }

    private GoogleMapsResponse getGoogleMapsResponse(Address origin, Address destination) {
        try {
            // Prepare the parameters for the Google Maps API request
            String originParam = origin.getLatitude() + "," + origin.getLongitude();
            String destinationParam = destination.getLatitude() + "," + destination.getLongitude();

            // Call Google Maps API and return the response
            return googleMapsService.getDistanceMatrix(originParam, destinationParam);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public double getInitialTemperature() {
        return initialTemperature;
    }

    public double getCoolingRate() {
        return coolingRate;
    }

}
