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

    public List<RouteLeg> calculateOptimalRoute(int addressLimit) {
        List<Address> allAddresses = addressService.findAllAddresses(addressLimit);
        List<RouteLeg> route = new ArrayList<>();
        Address origin = allAddresses.remove(0); // Starting point

        while (!allAddresses.isEmpty()) {
            RouteLeg optimalLeg = findOptimalDestination(origin, new ArrayList<>(allAddresses));
            route.add(optimalLeg);
            origin = getAddressById(optimalLeg.getDestinationId(), allAddresses);
            allAddresses.remove(origin);
        }

        return route;
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
                optimalLeg = new RouteLeg(destination.getId(), time, distance);
            }
        }

        return optimalLeg;
    }

    private Address getAddressById(Long id, List<Address> addresses) {
        // Method to find an Address object by ID from a list of addresses
        return addresses.stream().filter(a -> a.getId().equals(id)).findFirst().orElse(null);
    }

}
