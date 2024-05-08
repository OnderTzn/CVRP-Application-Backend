package com.example.cvrp.algorithms;

import com.example.cvrp.dto.RouteLeg;
import com.example.cvrp.model.Address;
import com.example.cvrp.model.GoogleMapsResponse;
import com.example.cvrp.service.GoogleMapsServiceImp;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class NearestNeighborAlgorithm implements RoutingAlgorithm {

    private final GoogleMapsServiceImp googleMapsService;

    public NearestNeighborAlgorithm(GoogleMapsServiceImp googleMapsService) {
        this.googleMapsService = googleMapsService;
    }


    @Override
    public List<RouteLeg> calculateRouteWithDepot(Address depot, List<Address> addresses, long vehicleCapacity) {
        //System.out.println("Starting calculateRouteWithDepot");
        if (addresses == null) {
            //System.out.println("Address list is null.");
            return new ArrayList<>(); // Return an empty route list
        } else if (addresses.isEmpty()) {
            //System.out.println("Address list is empty.");
            return new ArrayList<>(); // Return an empty route list
        }
        // Debug print to confirm addresses are received correctly
        //System.out.println("Received addresses for routing:");
        //addresses.forEach(address -> System.out.println("Address ID: " + address.getId() + ", Address Details: " + address.toString()));

        List<Address> tempAddresses = new ArrayList<>(addresses);
        List<RouteLeg> route = new ArrayList<>();
        tempAddresses.remove(depot);

        //System.out.println("Depot: " + depot.getId() + ", Addresses count: " + addresses.size());
        Long currentCapacity = vehicleCapacity;
        Address origin = depot;

        while (!tempAddresses.isEmpty()) {
            //System.out.println("Current origin: " + origin.getId() + ", Remaining addresses: " + tempAddresses.size() + ", Current capacity: " + currentCapacity);
            Address nextAddress = findFeasibleDestinationWithCapacity(origin, tempAddresses, currentCapacity);

            if (nextAddress == null) {
                // Return to depot if no feasible destination is found
                //System.out.println("No feasible destination found from origin: " + origin.getId() + ". Returning to depot.");
                RouteLeg legToDepot = createRouteLegToDepot(origin, depot);
                route.add(legToDepot);
                origin = depot;
                currentCapacity = vehicleCapacity;
                continue;
            }

            // Visit the next address
            //System.out.println("Next address to visit: " + nextAddress.getId());
            RouteLeg leg = createRouteLeg(origin, nextAddress);
            route.add(leg);
            currentCapacity -= nextAddress.getUnit();
            tempAddresses.remove(nextAddress);
            origin = nextAddress;
        }

        // Return to depot at the end of the route
        if (!origin.equals(depot)) {
            //System.out.println("Returning to depot from last address: " + origin.getId());
            route.add(createRouteLegToDepot(origin, depot));
        }

        System.out.println("Complete Route Legs:");
        for (RouteLeg leg : route) {
            //System.out.println("Inside for");
            System.out.println("Leg from ID: " + leg.getOriginId() + " to ID: " + leg.getDestinationId() +
                    ", Time: " + leg.getTime() + "s, Distance: " + leg.getDistance() + "m");
        }

        return route;
    }

    public List<RouteLeg> calculateRoute(List<Address> addresses, Long vehicleCapacity) {
        // Print the received addresses
        /*System.out.println("Received addresses:");
        for (Address address : addresses) {
            System.out.println("ID: " + address.getId() + ", Latitude: " + address.getLatitude() + ", Longitude: " + address.getLongitude());
        }*/

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
                    //System.out.println(i + ") Origin ID: " + origin.getId() + ", Destination ID: " + destination.getId() +
                    //        ", Time: " + time + "s, Distance: " + distance + "m");

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

}
