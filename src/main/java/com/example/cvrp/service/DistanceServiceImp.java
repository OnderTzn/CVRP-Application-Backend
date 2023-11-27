package com.example.cvrp.service;

import com.example.cvrp.model.Address;
import com.example.cvrp.model.GoogleMapsResponse;
import org.springframework.stereotype.Service;

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
        String origins = "52.5235445,13.4193129"; // Your starting point
        String destinations = addresses.stream()
                .map(a -> a.getLatitude() + "," + a.getLongitude())
                .collect(Collectors.joining("|"));

        return googleMapsService.getDistanceMatrix(origins, destinations);
    }
}
