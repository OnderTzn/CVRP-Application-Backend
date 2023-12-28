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

    public GoogleMapsResponse getDistanceAndTimeForAddresses() {
        List<Address> addresses = addressService.findAllAddresses();
        // Convert the list of addresses into a string format expected by Google Maps API
        // For example: "lat1,lon1|lat2,lon2|lat3,lon3"
        String origins = "52.5235445,13.4193129"; // Starting point
        String destinations = "52.5194301,13.3960675" /*addresses.stream()
                .map(a -> a.getLatitude() + "," + a.getLongitude())
                .collect(Collectors.joining("|"))*/;

        return googleMapsService.getDistanceMatrix(origins, destinations);
    }


    public List<RouteLeg> calculateOptimalRoute() {
        List<Address> allAddresses = addressService.findAllAddresses();
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
        // Logic to compare destinations and find the optimal one
        // Use GoogleMapsService here to get time and distance
        return optimalLeg;
    }

    private Address getAddressById(Long id, List<Address> addresses) {
        // Method to find an Address object by ID from a list of addresses
        return addresses.stream().filter(a -> a.getId().equals(id)).findFirst().orElse(null);
    }

}
