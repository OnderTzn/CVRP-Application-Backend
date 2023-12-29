package com.example.cvrp.service;

import com.example.cvrp.dto.RouteLeg;
import com.example.cvrp.model.Address;
import com.example.cvrp.model.GoogleMapsResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DistanceServiceImp {
    private final GoogleMapsServiceImp googleMapsService;
    private final AddressService addressService;

    public DistanceServiceImp(GoogleMapsServiceImp googleMapsService, AddressService addressService) {
        this.googleMapsService = googleMapsService;
        this.addressService = addressService;
    }

    public List<RouteLeg> calculateOptimalRoute(int addressLimit, Long vehicleCapacity) {
        // Increment addressLimit by 1 to account for the depot
        addressLimit = addressLimit + 1;

        List<Address> allAddresses = new ArrayList<>(addressService.findAllAddresses(addressLimit));
        List<RouteLeg> route = new ArrayList<>();
        Address depot = allAddresses.get(0);   // Store the starting point

        // Add the depot as the starting point in the route
        route.add(new RouteLeg(depot.getId(), depot.getLatitude(), depot.getLongitude()));

        Long currentCapacity = vehicleCapacity;
        Address origin = depot;

        // Remove depot from allAddresses to avoid considering it as a next stop
        allAddresses.remove(depot);

        while (!allAddresses.isEmpty()) {
            Address nextAddress = findFeasibleDestinationWithCapacity(origin, allAddresses, currentCapacity);

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
            allAddresses.remove(nextAddress);
            origin = nextAddress;
        }

        // Return to depot at the end of the route
        if (!origin.equals(depot)) {
            route.add(createRouteLegToDepot(origin, depot));
        }

        return route;
    }


    private Address findFeasibleDestinationWithCapacity(Address origin, List<Address> potentialDestinations, Long currentCapacity) {
        Address optimalDestination = null;
        Double shortestTime = Double.MAX_VALUE;
        Double shortestDistance = Double.MAX_VALUE;

        for (Address destination : potentialDestinations) {
            if (!destination.equals(origin) && destination.getUnit() <= currentCapacity) {
                GoogleMapsResponse response = getGoogleMapsResponse(origin, destination);
                Double time = response.getRows().get(0).getElements().get(0).getDuration().getValue();
                Double distance = response.getRows().get(0).getElements().get(0).getDistance().getValue();

                if (time < shortestTime || (time.equals(shortestTime) && distance < shortestDistance)) {
                    shortestTime = time;
                    shortestDistance = distance;
                    optimalDestination = destination;
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

        return new RouteLeg(destination.getId(), destination.getLatitude(), destination.getLongitude(), time, distance);
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
                        destination.getId(),
                        destination.getLatitude(),
                        destination.getLongitude(),
                        time,
                        distance);
            }
        }

        return optimalLeg;
    }

    private Address getAddressById(Long id, List<Address> addresses) {
        // Method to find an Address object by ID from a list of addresses
        return addresses.stream().filter(a -> a.getId().equals(id)).findFirst().orElse(null);
    }

}
