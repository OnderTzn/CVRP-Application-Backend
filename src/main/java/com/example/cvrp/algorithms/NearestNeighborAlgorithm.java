package com.example.cvrp.algorithms;

import com.example.cvrp.dto.RouteLeg;
import com.example.cvrp.model.Address;
import com.example.cvrp.model.GoogleMapsResponse;
import com.example.cvrp.model.TimeDistance;
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
    public List<RouteLeg> calculateRoute(Address depot, List<Address> addresses, long vehicleCapacity) {
        if (addresses == null || addresses.isEmpty()) {
            return new ArrayList<>(); // Return an empty route list
        }

        List<Address> tempAddresses = new ArrayList<>(addresses);
        //tempAddresses.remove(depot);

        // Step 1: Create the initial route without considering capacity
        List<Address> initialRoute = createInitialRoute(depot, tempAddresses);

        // Step 2: Adjust the route for capacity
        return convertToRouteLegs(initialRoute, depot, vehicleCapacity);
    }

    @Override
    public List<RouteLeg> calculateRoute(List<Address> addresses, Long vehicleCapacity) {
        if (addresses == null || addresses.isEmpty()) {
            return new ArrayList<>(); // Return an empty route list
        }

        List<Address> tempAddresses = new ArrayList<>(addresses);
        Address depot = tempAddresses.get(0); // Assume the first address is the depot
        tempAddresses.remove(depot);

        // Step 1: Create the initial route without considering capacity
        List<Address> initialRoute = createInitialRoute(depot, tempAddresses);

        // Step 2: Adjust the route for capacity
        return convertToRouteLegs(initialRoute, depot, vehicleCapacity);
    }

    private List<Address> createInitialRoute(Address depot, List<Address> addresses) {
        List<Address> route = new ArrayList<>();
        Address origin = depot;
        List<Address> tempAddresses = new ArrayList<>(addresses);

        route.add(depot); // Start from the depot

        while (!tempAddresses.isEmpty()) {
            Address nextAddress = findNearestNeighbor(origin, tempAddresses);
            if (nextAddress != null) {
                route.add(nextAddress);
                tempAddresses.remove(nextAddress);
                origin = nextAddress;
            }
        }

        return route;
    }

    private Address findNearestNeighbor(Address origin, List<Address> potentialDestinations) {
        Address nearestNeighbor = null;
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
                        nearestNeighbor = destination;
                    }
                }
            }
        }
        return nearestNeighbor;
    }

    private GoogleMapsResponse getGoogleMapsResponse(Address origin, Address destination) {
        try {
            String originParam = origin.getLatitude() + "," + origin.getLongitude();
            String destinationParam = destination.getLatitude() + "," + destination.getLongitude();
            return googleMapsService.getDistanceMatrix(originParam, destinationParam);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private List<RouteLeg> convertToRouteLegs(List<Address> initialRoute, Address depot, long vehicleCapacity) {
        List<RouteLeg> routeLegs = new ArrayList<>();
        Long currentCapacity = vehicleCapacity;

        for (int i = 0; i < initialRoute.size() - 1; i++) {
            Address from = initialRoute.get(i);
            Address to = initialRoute.get(i + 1);
            Long remainingDemand = to.getUnit(); // Make a copy of the demand

            while (remainingDemand > 0) {
                if (remainingDemand > currentCapacity) {
                    routeLegs.addAll(deliverUnits(from, to, currentCapacity));
                    remainingDemand -= currentCapacity;
                    currentCapacity = vehicleCapacity;
                    routeLegs.addAll(returnToDepotAndRefill(to, depot));
                    from = depot;
                } else {
                    routeLegs.addAll(deliverUnits(from, to, remainingDemand));
                    currentCapacity -= remainingDemand;
                    remainingDemand = 0L;
                }
            }

            // If the current capacity is 0 after unloading, return to the depot to refill before proceeding
            if (currentCapacity == 0 && !to.equals(depot)) {
                routeLegs.addAll(returnToDepotAndRefill(to, depot));
                currentCapacity = vehicleCapacity;

                // Add leg from depot to next address with the next address's demand
                if (i < initialRoute.size() - 2) {
                    Address nextTo = initialRoute.get(i + 2);
                    routeLegs.addAll(addLegFromDepotToNextAddress(depot, nextTo, nextTo.getUnit()));
                    i++;
                }
            }
        }

        // Ensure the last leg returns to the depot
        addFinalLegToDepot(initialRoute, depot, routeLegs);

        System.out.println("Final Route:");
        for (RouteLeg leg : routeLegs) {
            System.out.println("From ID: " + leg.getOriginId() + " To ID: " + leg.getDestinationId() +
                    " - Distance: " + leg.getDistance() + "m, Time: " + leg.getTime() + "s, Capacity Used: " + leg.getVehicleCapacity() + " units");
        }

        return routeLegs;
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

    private void addFinalLegToDepot(List<Address> initialRoute, Address depot, List<RouteLeg> routeLegs) {
        Address lastAddress = initialRoute.get(initialRoute.size() - 1);
        if (!lastAddress.equals(depot)) {
            TimeDistance backToDepot = getTimeDistanceBetweenAddresses(lastAddress, depot);
            routeLegs.add(new RouteLeg(lastAddress.getId(), depot.getId(), lastAddress.getLatitude(), lastAddress.getLongitude(),
                    depot.getLatitude(), depot.getLongitude(), backToDepot.getTime(), backToDepot.getDistance(), 0L));
        }
    }

    private TimeDistance getTimeDistanceBetweenAddresses(Address from, Address to) {
        GoogleMapsResponse response = getGoogleMapsResponse(from, to);
        if (response == null || response.getRows() == null || response.getRows().isEmpty()) {
            return new TimeDistance(Double.MAX_VALUE, Double.MAX_VALUE); // Handle error
        }

        Double time = response.getRows().get(0).getElements().get(0).getDuration().getValue();
        Double distance = response.getRows().get(0).getElements().get(0).getDistance().getValue();

        return new TimeDistance(time, distance);
    }
}
