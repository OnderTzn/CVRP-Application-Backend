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

    public List<RouteLeg> calculateOptimalRoute(int addressLimit, int vehicleCapacity) {
        List<Address> allAddresses = new ArrayList<>(addressService.findAllAddresses(addressLimit));
        List<RouteLeg> route = new ArrayList<>();

        Address depot = allAddresses.get(0);
        int currentCapacity = vehicleCapacity;
        Address origin = depot;

        while (!allAddresses.isEmpty()) {
            Address nextAddress = findFeasibleDestinationWithCapacity(origin, allAddresses, currentCapacity);

            if (nextAddress == null) {
                route.add(createRouteLegToDepot(origin, depot));
                currentCapacity = vehicleCapacity;
                origin = depot;
            }
            else {
                RouteLeg leg = createRouteLeg(origin, nextAddress);
                route.add(leg);
                currentCapacity -= nextAddress.getUnit();
                allAddresses.remove(nextAddress);
                origin = nextAddress;
            }
        }

        return route;
    }

    private RouteLeg createRouteLegToDepot(Address origin, Address depot) {
        return getRouteLeg(origin, depot);
    }


    private Address findFeasibleDestinationWithCapacity(Address origin, List<Address> potentialDestinations, int currentCapacity) {
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

    private RouteLeg createRouteLeg(Address origin, Address destination) {
        return getRouteLeg(origin, destination);
    }

    private RouteLeg getRouteLeg(Address origin, Address destination) {
        GoogleMapsResponse response = getGoogleMapsResponse(origin, destination);
        Double time = response.getRows().get(0).getElements().get(0).getDuration().getValue();
        Double distance = response.getRows().get(0).getElements().get(0).getDistance().getValue();

        return new RouteLeg(destination.getId(),
                destination.getLatitude(),
                destination.getLongitude(),
                time,
                distance);
    }

    private GoogleMapsResponse getGoogleMapsResponse(Address origin, Address destination) {
        String originParam = origin.getLatitude() + "," + origin.getLongitude();
        String destinationParam = destination.getLatitude() + "," + destination.getLongitude();

        return googleMapsService.getDistanceMatrix(originParam, destinationParam);
    }

    private Address getAddressById(Long id, List<Address> addresses) {
        // Method to find an Address object by ID from a list of addresses
        return addresses.stream().filter(a -> a.getId().equals(id)).findFirst().orElse(null);
    }

}
