package com.example.cvrp.algorithms;

import com.example.cvrp.dto.RouteLeg;
import com.example.cvrp.model.Address;
import com.example.cvrp.model.Saving;
import com.example.cvrp.model.GoogleMapsResponse;
import com.example.cvrp.model.TimeDistance;
import com.example.cvrp.service.GoogleMapsServiceImp;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Comparator;

public class SavingsAlgorithm implements RoutingAlgorithm {

    private final GoogleMapsServiceImp googleMapsService;

    public SavingsAlgorithm(GoogleMapsServiceImp googleMapsService) {
        this.googleMapsService = googleMapsService;
    }

    public List<RouteLeg> calculateRoute(List<Address> addresses, Long vehicleCapacity) {
        System.out.println("Received addresses from Savings Algorithm:");
        for (Address address : addresses) {
            System.out.println("ID: " + address.getId() + ", Latitude: " + address.getLatitude() + ", Longitude: " + address.getLongitude());
        }

        // Assume the first address is the depot
        Address depot = addresses.get(0);

        // Initialize individual routes from depot to each customer and back
        List<List<Address>> routes = initializeRoutes(addresses);

        // Calculate savings for all pairs of customers
        PriorityQueue<Saving> savingsQueue = calculateSavings(addresses, depot);

        // Merge routes based on savings
        mergeRoutes(savingsQueue, routes);

        // Convert the list of addresses in routes to RouteLegs
        return convertToRouteLegs(routes, vehicleCapacity);
    }

    private List<List<Address>> initializeRoutes(List<Address> addresses) {
        List<List<Address>> routes = new ArrayList<>();
        Address depot = addresses.get(0); // Assuming the depot is the first address
        for (int i = 1; i < addresses.size(); i++) {
            List<Address> route = new ArrayList<>();
            route.add(depot); // Start from the depot
            route.add(addresses.get(i)); // Go to customer
            route.add(depot); // Return to the depot
            routes.add(route);
        }
        return routes;
    }


    private PriorityQueue<Saving> calculateSavings(List<Address> addresses, Address depot) {
        PriorityQueue<Saving> savingsQueue = new PriorityQueue<>(Comparator.comparing(Saving::getSaving).reversed());
        for (int i = 1; i < addresses.size(); i++) {
            for (int j = i + 1; j < addresses.size(); j++) {
                double saving = calculateSaving(depot, addresses.get(i), addresses.get(j));
                savingsQueue.add(new Saving(addresses.get(i), addresses.get(j), saving));
            }
        }
        return savingsQueue;
    }

    private double calculateSaving(Address depot, Address a, Address b) {
        TimeDistance depotToA = getTravelTime(depot, a);
        TimeDistance depotToB = getTravelTime(depot, b);
        TimeDistance aToB = getTravelTime(a, b);

        // Prioritize time savings
        return depotToA.getTime() + depotToB.getTime() - aToB.getTime();
    }


    private void mergeRoutes(PriorityQueue<Saving> savingsQueue, List<List<Address>> routes) {
        while (!savingsQueue.isEmpty()) {
            Saving saving = savingsQueue.poll();

            List<Address> route1 = findRouteContaining(routes, saving.getAddress1());
            List<Address> route2 = findRouteContaining(routes, saving.getAddress2());

            if (route1 != null && route2 != null && !route1.equals(route2) && canBeMerged(route1, route2)) {
                mergeTwoRoutes(route1, route2, routes);
            }
        }
    }


    private List<RouteLeg> convertToRouteLegs(List<List<Address>> routes, Long vehicleCapacity) {
        List<RouteLeg> routeLegs = new ArrayList<>();
        Long currentCapacity = vehicleCapacity;
        Address depot = routes.get(0).get(0); // Assuming the depot is the first address of the first route

        for (List<Address> route : routes) {
            for (int i = 0; i < route.size() - 1; i++) {
                Address from = route.get(i);
                Address to = route.get(i + 1);

                if (to.getUnit() > currentCapacity) {
                    // Add leg back to depot
                    routeLegs.add(createRouteLegBetweenAddresses(from, depot));
                    // Reset capacity
                    currentCapacity = vehicleCapacity;
                    // Add leg from depot to 'to'
                    routeLegs.add(createRouteLegBetweenAddresses(depot, to));
                } else {
                    // Add leg from 'from' to 'to'
                    routeLegs.add(createRouteLegBetweenAddresses(from, to));
                }
                currentCapacity -= to.getUnit();
            }
        }
        /* Maybe redundant, current capacity is always => 0 and depot has 0 demand
        // Make sure the last leg returns to the depot if not already there
        Long lastDestinationId = routeLegs.get(routeLegs.size() - 1).getDestinationId();
        Address lastAddress = findAddressById(lastDestinationId, addresses);
        if (lastDestinationId != null && !lastDestinationId.equals(depot.getId())) {
            routeLegs.add(createRouteLegBetweenAddresses(depot, depot));
        }*/

        // Print the routeLegs before returning
        System.out.println("Complete Route Legs:");
        for (RouteLeg leg : routeLegs) {
            System.out.println("Leg from ID: " + leg.getOriginId() + " to ID: " + leg.getDestinationId() +
                    ", Time: " + leg.getTime() + "s, Distance: " + leg.getDistance() + "m");
        }

        return routeLegs;
    }

    private RouteLeg createRouteLegBetweenAddresses(Address from, Address to) {
        TimeDistance timeDistance = getTravelTime(from, to);
        return new RouteLeg(from.getId(), to.getId(),
                from.getLatitude(), from.getLongitude(),
                to.getLatitude(), to.getLongitude(),
                timeDistance.getTime(), timeDistance.getDistance());
    }



    // Helper Methods
    private TimeDistance getTravelTime(Address from, Address to) {
        // Fetch the travel time between origin and destination using Google Maps API
        // through the googleMapsService and return the time.
        GoogleMapsResponse response = googleMapsService.getDistanceMatrix(
                from.getLatitude() + "," + from.getLongitude(),
                to.getLatitude() + "," + to.getLongitude()
        );

        if (response == null || response.getRows().isEmpty() || response.getRows().get(0).getElements().isEmpty()) {
            return new TimeDistance(Double.MAX_VALUE, Double.MAX_VALUE); // Handle error scenario
        }

        Double time = response.getRows().get(0).getElements().get(0).getDuration().getValue();
        Double distance = response.getRows().get(0).getElements().get(0).getDistance().getValue();

        return new TimeDistance(time, distance);
    }

    // Search through all routes to find the one that contains the specified address
    private List<Address> findRouteContaining(List<List<Address>> routes, Address address) {
        // Find and return the route containing the specified address
        for (List<Address> route : routes) {
            if (route.contains(address)) {
                return route;
            }
        }
        return null; // Return null if no route contains the address
    }

    private boolean canBeMerged(List<Address> route1, List<Address> route2) {
        // Check if two routes can be merged based specific constraints
        return !route1.equals(route2);
    }

    private void mergeTwoRoutes(List<Address> route1, List<Address> route2, List<List<Address>> allRoutes) {
        // Merge route2 into route1 and remove route2 from allRoutes
        // Assuming the last address of route1 and the first address of route2 are the depot
        route1.remove(route1.size() - 1); // Remove the last address (depot) from route1
        route2.remove(0); // Remove the first address (depot) from route2

        // Append route2 to route1
        route1.addAll(route2);

        // Remove the merged route2 from the list of all routes
        allRoutes.remove(route2);
    }

    private Address findAddressById(Long id, List<Address> addresses) {
        for (Address address : addresses) {
            if (address.getId().equals(id)) {
                return address;
            }
        }
        return null; // Handle this case appropriately
    }



}
