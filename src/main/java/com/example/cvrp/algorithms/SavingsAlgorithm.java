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

    @Override
    public List<RouteLeg> calculateRoute(Address depot, List<Address> addresses, long vehicleCapacity) {
        // Initialize individual routes from depot to each customer and back
        List<List<Address>> routes = initializeRoutes(addresses, depot);

        // Calculate savings for all pairs of addresses
        PriorityQueue<Saving> savingsQueue = calculateSavings(addresses, depot);

        // Merge routes based on savings
        mergeRoutes(savingsQueue, routes);

        // Convert the list of addresses in routes to RouteLegs
        return convertToRouteLegs(routes, depot, vehicleCapacity);
    }

    public List<RouteLeg> calculateRoute(List<Address> addresses, Long vehicleCapacity) {
        // Assume the first address is the depot
        Address depot = addresses.get(0);

        // Initialize individual routes from depot to each customer and back
        List<List<Address>> routes = initializeRoutes(addresses, depot);

        // Calculate savings for all pairs of customers
        PriorityQueue<Saving> savingsQueue = calculateSavings(addresses, depot);

        // Merge routes based on savings
        mergeRoutes(savingsQueue, routes);

        // Convert the list of addresses in routes to RouteLegs
        return convertToRouteLegs(routes, depot, vehicleCapacity);
    }

    private List<List<Address>> initializeRoutes(List<Address> addresses, Address depot) {
        List<List<Address>> routes = new ArrayList<>();
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

    private List<RouteLeg> convertToRouteLegs(List<List<Address>> routes, Address depot, Long vehicleCapacity) {
        List<RouteLeg> routeLegs = new ArrayList<>();
        Long currentCapacity = vehicleCapacity;

        for (List<Address> route : routes) {
            for (int i = 0; i < route.size() - 1; i++) {
                Address from = route.get(i);
                Address to = route.get(i + 1);
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
                    if (i < route.size() - 2) {
                        Address nextTo = route.get(i + 2);
                        routeLegs.addAll(addLegFromDepotToNextAddress(depot, nextTo, nextTo.getUnit()));
                        i++;
                    }
                }
            }
        }

        // Ensure the last leg returns to the depot
        addFinalLegToDepot(routes, depot, routeLegs);

        System.out.println("Final Route:");
        for (RouteLeg leg : routeLegs) {
            System.out.println("From ID: " + leg.getOriginId() + " To ID: " + leg.getDestinationId() +
                    " - Distance: " + leg.getDistance() + "m, Time: " + leg.getTime() + "s, Capacity Used: " + leg.getVehicleCapacity() + " units");
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

    private List<RouteLeg> deliverUnits(Address from, Address to, Long units) {
        List<RouteLeg> routeLegs = new ArrayList<>();
        TimeDistance timeDistance = getTravelTime(from, to);
        routeLegs.add(new RouteLeg(from.getId(), to.getId(), from.getLatitude(), from.getLongitude(),
                to.getLatitude(), to.getLongitude(), timeDistance.getTime(), timeDistance.getDistance(), units));
        return routeLegs;
    }

    private List<RouteLeg> returnToDepotAndRefill(Address from, Address depot) {
        List<RouteLeg> routeLegs = new ArrayList<>();
        TimeDistance backToDepot = getTravelTime(from, depot);
        routeLegs.add(new RouteLeg(from.getId(), depot.getId(), from.getLatitude(), from.getLongitude(),
                depot.getLatitude(), depot.getLongitude(), backToDepot.getTime(), backToDepot.getDistance(), 0L));
        return routeLegs;
    }

    private List<RouteLeg> addLegFromDepotToNextAddress(Address depot, Address nextTo, Long units) {
        List<RouteLeg> routeLegs = new ArrayList<>();
        TimeDistance fromDepot = getTravelTime(depot, nextTo);
        routeLegs.add(new RouteLeg(depot.getId(), nextTo.getId(), depot.getLatitude(), depot.getLongitude(),
                nextTo.getLatitude(), nextTo.getLongitude(), fromDepot.getTime(), fromDepot.getDistance(), units));
        return routeLegs;
    }

    private void addFinalLegToDepot(List<List<Address>> routes, Address depot, List<RouteLeg> routeLegs) {
        List<Address> lastRoute = routes.get(routes.size() - 1);
        //lastRoute consists the addresses. To create a leg to depot, we need to get the address before the depot. Hence, it's size() -2
        Address lastAddress = lastRoute.get(lastRoute.size() - 2);

        if (!lastAddress.equals(depot)) {
            TimeDistance backToDepot = getTravelTime(lastAddress, depot);
            routeLegs.add(new RouteLeg(lastAddress.getId(), depot.getId(), lastAddress.getLatitude(), lastAddress.getLongitude(),
                    depot.getLatitude(), depot.getLongitude(), backToDepot.getTime(), backToDepot.getDistance(), 0L));
        }
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

}
