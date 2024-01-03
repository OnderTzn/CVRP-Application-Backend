package com.example.cvrp.algorithms;

import com.example.cvrp.dto.RouteLeg;
import com.example.cvrp.model.Address;
import com.example.cvrp.model.GoogleMapsResponse;
import com.example.cvrp.model.TimeDistance;
import com.example.cvrp.service.GoogleMapsServiceImp;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Map;
import java.util.HashMap;

public class DijkstraAlgorithm implements RoutingAlgorithm{

    private final GoogleMapsServiceImp googleMapsService;
    private Map<Long, Address> addressMap;

    public DijkstraAlgorithm(GoogleMapsServiceImp googleMapsService) {
        this.googleMapsService = googleMapsService;
    }

    public List<RouteLeg> calculateRoute(List<Address> addresses, Long vehicleCapacity) {
        this.addressMap = new HashMap<>();
        for (Address address : addresses) {
            addressMap.put(address.getId(), address);
        }
        // Print the received addresses
        System.out.println("Received addresses:");
        for (Address address : addresses) {
            System.out.println("ID: " + address.getId() + ", Latitude: " + address.getLatitude() + ", Longitude: " + address.getLongitude());
        }

        // Initialize variables
        Address depot = addresses.get(0);   // Store the starting point
        System.out.println("DEPOT  ID: " + depot.getId() + ", Latitude: " + depot.getLatitude() + ", Longitude: " + depot.getLongitude());
        PriorityQueue<Node> priorityQueue = new PriorityQueue<>();
        priorityQueue.add(new Node(depot.getId(), new TimeDistance(0, 0))); // Assuming depot is your starting point

        Map<Long, TimeDistance> shortestTimeToAddress = new HashMap<>();
        Map<Long, Long> previousAddress = new HashMap<>();

        Long currentCapacity = vehicleCapacity;
        System.out.println("Above of the 44 - while (!priorityQueue.isEmpty())");

        while (!priorityQueue.isEmpty()) {
            Node currentNode = priorityQueue.poll();
            System.out.println("Address polled: " + currentNode.addressId);
            System.out.println("TimeDistance of address polled: " + currentNode.timeDistance);
            if (!addressMap.containsKey(currentNode.addressId)) {
                System.out.println("51 - Address ID not found in map: " + currentNode.addressId);
                continue;
            }
            Address currentAddress = addressMap.get(currentNode.addressId);
            if (currentAddress == null) {
                System.out.println("59 - Current address is null for ID: " + currentNode.addressId);
                continue;
            }
            TimeDistance currentTimeDistance = currentNode.timeDistance;

            // Check for capacity and refill if necessary
            System.out.println("Above of the 52 - if (currentCapacity < currentAddress.getUnit())");
            if (currentCapacity < currentAddress.getUnit()) {
                // Not enough capacity to visit the current address, return to depot to refill
                // Calculate the time and distance to return to the depot
                TimeDistance timeDistanceToDepot = getTimeDistanceBetweenAddresses(currentAddress, depot);
                TimeDistance totalTimeDistanceToDepot = currentTimeDistance.add(timeDistanceToDepot);

                // Update the maps for time distance and previous address
                shortestTimeToAddress.put(depot.getId(), totalTimeDistanceToDepot);
                previousAddress.put(depot.getId(), currentAddress.getId());

                // Reset current capacity and add depot back to the priority queue
                currentCapacity = vehicleCapacity;

                // Add the depot back to the priority queue
                priorityQueue.add(new Node(depot.getId(), totalTimeDistanceToDepot));
                // May need additional logic to ensure correct routing
            }
            else {
                currentCapacity -= currentAddress.getUnit();
            }
            System.out.println("Above of the 73 for (Address neighbor : addressMap.values())");
            for (Address neighbor : addressMap.values()) {
                System.out.println("Processing addresses inside for loop:" + currentAddress.getId());
                System.out.println("Neighbor:" + neighbor.getId());
                if (neighbor.getId().equals(currentAddress.getId())) continue; // Skip the same address

                // Get time and distance from currentAddress to neighbor
                TimeDistance timeDistanceToNeighbor = getTimeDistanceBetweenAddresses(currentAddress, neighbor);
                TimeDistance totalTimeDistance = currentTimeDistance.add(timeDistanceToNeighbor);

                if (!shortestTimeToAddress.containsKey(neighbor.getId()) ||
                        totalTimeDistance.isBetterThan(shortestTimeToAddress.get(neighbor.getId()))) {
                    shortestTimeToAddress.put(neighbor.getId(), totalTimeDistance);
                    previousAddress.put(neighbor.getId(), currentAddress.getId());
                    priorityQueue.add(new Node(neighbor.getId(), totalTimeDistance));
                }
            }
        }
        System.out.println("DEPOT ID BEFORE SENDING IT TO CONSTRUCT:" + depot.getId());
        // Construct the route from the shortest paths found
        return constructRouteFromShortestPaths(depot.getId(), previousAddress, shortestTimeToAddress);
    }

    private TimeDistance getTimeDistanceBetweenAddresses(Address origin, Address destination) {
        GoogleMapsResponse response = googleMapsService.getDistanceMatrix(
                origin.getLatitude() + "," + origin.getLongitude(),
                destination.getLatitude() + "," + destination.getLongitude());

        Double time = response.getRows().get(0).getElements().get(0).getDuration().getValue();
        Double distance = response.getRows().get(0).getElements().get(0).getDistance().getValue();

        return new TimeDistance(time, distance);
    }

    private List<RouteLeg> constructRouteFromShortestPaths(Long depotId, Map<Long, Long> previousAddress, Map<Long, TimeDistance> shortestTimeToAddress) {
        List<RouteLeg> route = new ArrayList<>();

        // Start with the depot as the first point in the route
        Address depot = addressMap.get(depotId);
        route.add(new RouteLeg(depotId, depot.getLatitude(), depot.getLongitude()));

        for (Map.Entry<Long, TimeDistance> entry : shortestTimeToAddress.entrySet()) {
            Long addressId = entry.getKey();
            TimeDistance timeDistance = entry.getValue();

            // Skip the depot since it's already added as the starting point
            if (addressId.equals(depotId)) {
                continue;
            }

            // Backtrack to find the path to the depot
            List<Long> path = new ArrayList<>();
            Long current = addressId;
            while (current != null && !current.equals(depotId)) {
                path.add(0, current); // Add to the front of the list
                current = previousAddress.get(current);
            }

            // Construct RouteLeg for each segment of the path
            Long prevAddressId = depotId;
            for (Long nextAddressId : path) {
                Address prevAddress = addressMap.get(prevAddressId);
                Address nextAddress = addressMap.get(nextAddressId);

                route.add(new RouteLeg(prevAddress.getId(), nextAddress.getId(),
                        prevAddress.getLatitude(), prevAddress.getLongitude(),
                        nextAddress.getLatitude(), nextAddress.getLongitude(),
                        timeDistance.getTime(), timeDistance.getDistance()));

                prevAddressId = nextAddressId; // Update for next iteration
            }
        }
        return route;
    }



    static class Node implements Comparable<Node> {
        Long addressId;
        TimeDistance timeDistance;

        Node(Long addressId, TimeDistance timeDistance) {
            this.addressId = addressId;
            this.timeDistance = timeDistance;
        }

        @Override
        public int compareTo(Node o) {
            return this.timeDistance.isBetterThan(o.timeDistance) ? -1 : 1;
        }
    }
}
