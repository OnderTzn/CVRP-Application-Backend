package com.example.cvrp.algorithms;

import com.example.cvrp.dto.RouteLeg;
import com.example.cvrp.model.Address;
import com.example.cvrp.model.GoogleMapsResponse;
import com.example.cvrp.service.GoogleMapsServiceImp;

import java.util.ArrayList;
import java.util.List;

public class NearestNeighborAlgorithm implements RoutingAlgorithm {

    private final GoogleMapsServiceImp googleMapsService;

    public NearestNeighborAlgorithm(GoogleMapsServiceImp googleMapsService) {
        this.googleMapsService = googleMapsService;
    }

    public List<RouteLeg> calculateRoute(List<Address> addresses, Long vehicleCapacity) {
        // Print the received addresses
        System.out.println("Received addresses:");
        for (Address address : addresses) {
            System.out.println("ID: " + address.getId() + ", Latitude: " + address.getLatitude() + ", Longitude: " + address.getLongitude());
        }

        List<Address> tempAddresses = new ArrayList<>(addresses);
        List<RouteLeg> route = new ArrayList<>();
        Address depot = tempAddresses.get(0);   // Store the starting point
        tempAddresses.remove(depot);

        Long currentCapacity = vehicleCapacity;
        Address origin = depot;

        while (!tempAddresses.isEmpty()) {
            Address nextAddress = findFeasibleDestinationWithCapacity(origin, tempAddresses, currentCapacity);

            if (nextAddress == null) {
                // Return to depot if no feasible destination is found
                RouteLeg legToDepot = createRouteLegToDepot(origin, depot);
                route.add(legToDepot);
                origin = depot;
                currentCapacity = vehicleCapacity;
                continue;
            }

            // Visit the next address
            RouteLeg leg = createRouteLeg(origin, nextAddress);
            route.add(leg);
            currentCapacity -= nextAddress.getUnit();
            tempAddresses.remove(nextAddress);
            origin = nextAddress;
        }

        // Return to depot at the end of the route
        if (!origin.equals(depot)) {
            route.add(createRouteLegToDepot(origin, depot));
        }
        System.out.println("Complete Route Legs:");
        for (RouteLeg leg : route) {
            System.out.println("Leg from ID: " + leg.getOriginId() + " to ID: " + leg.getDestinationId() +
                    ", Time: " + leg.getTime() + "s, Distance: " + leg.getDistance() + "m");
        }

        return route;
    }

    private Address findFeasibleDestinationWithCapacity(Address origin, List<Address> potentialDestinations, Long currentCapacity) {
        Address optimalDestination = null;
        Double shortestTime = Double.MAX_VALUE;
        Double shortestDistance = Double.MAX_VALUE;
        int i=1;
        for (Address destination : potentialDestinations) {
            if (!destination.equals(origin) && destination.getUnit() <= currentCapacity) {
                GoogleMapsResponse response = getGoogleMapsResponse(origin, destination);
                if (response != null && response.getRows() != null && !response.getRows().isEmpty()) {
                    Double time = response.getRows().get(0).getElements().get(0).getDuration().getValue();
                    Double distance = response.getRows().get(0).getElements().get(0).getDistance().getValue();


                    // Debug print
                    System.out.println(i + ") Origin ID: " + origin.getId() + ", Destination ID: " + destination.getId() +
                            ", Time: " + time + "s, Distance: " + distance + "m");

                    i++;
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

    private RouteLeg extractTimeAndDistance(Address origin, Address destination) {
        GoogleMapsResponse response = getGoogleMapsResponse(origin, destination);
        if (response == null || response.getRows() == null || response.getRows().isEmpty()) {
            // Handle the error scenario, possibly by logging and returning null or a default RouteLeg
            System.out.println("Error: Unable to get response from Google Maps for " + origin + " to " + destination);
            return null;
        }

        Double time = response.getRows().get(0).getElements().get(0).getDuration().getValue();
        Double distance = response.getRows().get(0).getElements().get(0).getDistance().getValue();

        return new RouteLeg(origin.getId(), destination.getId(),
                origin.getLatitude(), origin.getLongitude(),
                destination.getLatitude(), destination.getLongitude(),
                time, distance);
    }


    private RouteLeg createRouteLegToDepot(Address origin, Address depot) {
        return extractTimeAndDistance(origin, depot);
    }

    private RouteLeg createRouteLeg(Address origin, Address nextAddress) {
        return extractTimeAndDistance(origin, nextAddress);
    }

    private RouteLeg findOptimalDestination(Address origin, List<Address> potentialDestinations) {
        RouteLeg optimalLeg = null;
        Double shortestTime = Double.MAX_VALUE;
        Double shortestDistance = Double.MAX_VALUE;

        for (Address destination : potentialDestinations) {
            // Skip if destination is the same as origin
            if (destination.getId().equals(origin.getId())) {
                continue;
            }

            // Prepare the parameters for the Google Maps API request
            String originParam = origin.getLatitude() + "," + origin.getLongitude();
            String destinationParam = destination.getLatitude() + "," + destination.getLongitude();

            // Call Google Maps API via GoogleMapsService
            GoogleMapsResponse response = googleMapsService.getDistanceMatrix(originParam, destinationParam);

            // Extract time and distance from the response
            // Assume response structure matches your GoogleMapsResponse class
            Double time = response.getRows().get(0).getElements().get(0).getDuration().getValue();
            Double distance = response.getRows().get(0).getElements().get(0).getDistance().getValue();

            // Determine if this destination is the new optimal destination
            if (time < shortestTime || (time.equals(shortestTime) && distance < shortestDistance)) {
                shortestTime = time;
                shortestDistance = distance;
                optimalLeg = new RouteLeg(
                        origin.getId(),
                        destination.getId(),
                        destination.getLatitude(),
                        destination.getLongitude(),
                        time,
                        distance);
            }
        }

        return optimalLeg;
    }

}
