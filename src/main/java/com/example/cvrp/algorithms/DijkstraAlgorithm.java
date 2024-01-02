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

    public DijkstraAlgorithm(GoogleMapsServiceImp googleMapsService, List<Address> addresses) {
        this.googleMapsService = googleMapsService;
        this.addressMap = new HashMap<>();
        for (Address address : addresses) {
            addressMap.put(address.getId(), address);
        }
    }

    public List<RouteLeg> calculateRoute(List<Address> addresses, Long vehicleCapacity) {
        // Print the received addresses
        System.out.println("Received addresses:");
        for (Address address : addresses) {
            System.out.println("ID: " + address.getId() + ", Latitude: " + address.getLatitude() + ", Longitude: " + address.getLongitude());
        }

        // Initialize variables
        PriorityQueue<Node> priorityQueue = new PriorityQueue<>();
        Map<Long, TimeDistance> shortestTimeToAddress = new HashMap<>();
        Map<Long, Long> previousAddress = new HashMap<>();

        Long currentCapacity = vehicleCapacity;
        Address depot = addresses.get(0); // Depot is the first address

        while (!priorityQueue.isEmpty()) {
            Node currentNode = priorityQueue.poll();
            Address currentAddress = addressMap.get(currentNode.addressId);
            TimeDistance currentTimeDistance = currentNode.timeDistance;

            // Check for capacity and refill if necessary
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

            for (Address neighbor : addressMap.values()) {
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
        for (Map.Entry<Long, TimeDistance> entry : shortestTimeToAddress.entrySet()) {
            Long addressId = entry.getKey();
            TimeDistance timeDistance = entry.getValue();

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
