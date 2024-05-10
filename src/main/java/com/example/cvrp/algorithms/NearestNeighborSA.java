package com.example.cvrp.algorithms;

import com.example.cvrp.dto.RouteLeg;
import com.example.cvrp.model.Address;
import com.example.cvrp.model.GoogleMapsResponse;
import com.example.cvrp.model.TimeDistance;
import com.example.cvrp.service.GoogleMapsServiceImp;

import java.util.*;
import java.util.stream.Collectors;

public class NearestNeighborSA implements RoutingAlgorithm {

    private final GoogleMapsServiceImp googleMapsService;
    private Map<String, TimeDistance> distanceCache = new HashMap<>();
    private double temperature = 10000;
    private double coolingRate = 0.0001;
    private boolean allAddressesVisited = false;
    private int googleMapsRequestCount = 0;


    public NearestNeighborSA(GoogleMapsServiceImp googleMapsService) {
        this.googleMapsService = googleMapsService;
    }

    @Override
    public List<RouteLeg> calculateRouteWithDepot(Address depot, List<Address> addresses, long vehicleCapacity) {
        // Ensure the depot is included in the addresses list if not already
        if (!addresses.contains(depot)) {
            addresses.add(0, depot); // Add depot as the first address if it's not included
        }

        // Use Nearest Neighbor to get the initial route as addresses
        List<Address> currentSolution = generateInitialSolutionForSA(depot, addresses);

        List<Address> bestSolution = new ArrayList<>(currentSolution);

        System.out.println("Received addresses from SA:");
        for (Address address : addresses) {
            System.out.println("ID: " + address.getId() + ", Latitude: " + address.getLatitude() + ", Longitude: " + address.getLongitude());
        }

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

    @Override
    public List<RouteLeg> calculateRoute(List<Address> addresses, Long vehicleCapacity) {
        Address depot = findDepot(addresses);
        List<Address> currentSolution = generateInitialSolutionForSA(addresses);
        List<Address> bestSolution = new ArrayList<>(currentSolution);

        System.out.println("Received addresses from SA:");
        for (Address address : addresses) {
            System.out.println("ID: " + address.getId() + ", Latitude: " + address.getLatitude() + ", Longitude: " + address.getLongitude());
        }

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
            Address nextAddress = findFeasibleDestinationWithoutCapacity(currentAddress, tempAddresses);
            if (nextAddress == null) {
                // If no feasible next address found, it might indicate a logic error since capacity is not considered
                throw new IllegalStateException("No feasible next address found without considering capacity.");
            }

            // Add the next address to the route and update the current address
            routeAddresses.add(nextAddress);
            tempAddresses.remove(nextAddress);
            currentAddress = nextAddress;
        }

        // Ensure the route ends with the depot
        //if (!routeAddresses.get(routeAddresses.size() - 1).equals(depot)) {
        //    routeAddresses.add(depot);
        //}

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
            Address nextAddress = findFeasibleDestinationWithoutCapacity(currentAddress, tempAddresses);
            if (nextAddress == null) {
                // If no feasible next address found, it might indicate a logic error since capacity is not considered
                throw new IllegalStateException("No feasible next address found without considering capacity.");
            }

            // Add the next address to the route and update the current address
            routeAddresses.add(nextAddress);
            tempAddresses.remove(nextAddress);
            currentAddress = nextAddress;
        }

        // Ensure the route ends with the depot
        //if (!routeAddresses.get(routeAddresses.size() - 1).equals(depot)) {
        //    routeAddresses.add(depot);
        //}

        // Print the routeAddresses before returning
        System.out.println("Generated Initial Solution for SA:");
        for (Address address : routeAddresses) {
            System.out.println("Address ID: " + address.getId() + ", Latitude: " + address.getLatitude() + ", Longitude: " + address.getLongitude());
        }

        return routeAddresses;
    }

    private Address findFeasibleDestinationWithoutCapacity(Address origin, List<Address> potentialDestinations) {
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


}
