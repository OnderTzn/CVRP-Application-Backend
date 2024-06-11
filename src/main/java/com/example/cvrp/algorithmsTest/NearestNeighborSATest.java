package com.example.cvrp.algorithmsTest;

import com.example.cvrp.algorithms.RoutingAlgorithm;
import com.example.cvrp.dto.RouteLeg;
import com.example.cvrp.model.Address;
import com.example.cvrp.dto.TimeDistance;
import com.example.cvrp.service.DistanceMatrixServiceImp;

import java.util.*;

public class NearestNeighborSATest implements RoutingAlgorithm {

    private final DistanceMatrixServiceImp distanceMatrixService;
    private final double initialTemperature = 10000;
    private double temperature = initialTemperature;
    private double coolingRate = 0.01;


    public NearestNeighborSATest(DistanceMatrixServiceImp distanceMatrixService) {
        this.distanceMatrixService = distanceMatrixService;
    }

    @Override
    public List<RouteLeg> calculateRoute(Address depot, List<Address> addresses, long vehicleCapacity) {
        if (!addresses.contains(depot)) {
            addresses.add(0, depot);
        }
        temperature = initialTemperature;
        int addressCount = addresses.size();
        double coolingRate;

        // Determine the cooling rate based on the address count
        if (addressCount <= 16) {
            coolingRate = 0.01;
        } else if (addressCount <= 41) {
            coolingRate = 0.025;
        } else if (addressCount <= 101) {
            coolingRate = 0.025;
        } else {
            coolingRate = 0.025;
        }
        this.coolingRate = coolingRate;

        depot.setUnit(0L); // Ensure depot demand is 0

        List<Address> currentSolution = generateInitialSolutionForSA(depot, addresses);
        List<Address> bestSolution = new ArrayList<>(currentSolution);

        while (temperature > 1) {
            List<Address> newSolution = generateNeighborSolution(currentSolution, vehicleCapacity);

            double currentEnergy = calculateObjectiveValue(currentSolution, depot);
            double neighborEnergy = calculateObjectiveValue(newSolution, depot);

            if (acceptanceProbability(currentEnergy, neighborEnergy, temperature) > Math.random()) {
                currentSolution = new ArrayList<>(newSolution);
            }

            if (calculateObjectiveValue(currentSolution, depot) < calculateObjectiveValue(bestSolution, depot)) {
                bestSolution = new ArrayList<>(currentSolution);
            }

            temperature *= 1 - coolingRate;
        }

        return convertToRouteLegs(bestSolution, depot, vehicleCapacity);
    }

    @Override
    public List<RouteLeg> calculateRoute(List<Address> addresses, Long vehicleCapacity) {
        System.out.println("ON THE TEST FUNCTION");
        System.out.println("Nearest Neighbor SA TEST Algorithm");
        temperature = initialTemperature;
        int addressCount = addresses.size();
        double coolingRate;

        // Determine the cooling rate based on the address count
        if (addressCount <= 16) {
            coolingRate = 0.01;
        } else if (addressCount <= 41) {
            coolingRate = 0.025;
        } else if (addressCount <= 101) {
            coolingRate = 0.025;
        } else {
            coolingRate = 0.025;
        }
        this.coolingRate = coolingRate;

        Address depot = findDepot(addresses);
        depot.setUnit(0L); // Ensure depot demand is 0
        List<Address> currentSolution = generateInitialSolutionForSA(addresses);
        List<Address> bestSolution = new ArrayList<>(currentSolution);

        while (temperature > 1) {
            System.out.println("TEMPERATURE: " + temperature);
            List<Address> newSolution = generateNeighborSolution(currentSolution, vehicleCapacity);

            double currentEnergy = calculateObjectiveValue(currentSolution, depot);
            double neighborEnergy = calculateObjectiveValue(newSolution, depot);

            if (acceptanceProbability(currentEnergy, neighborEnergy, temperature) > Math.random()) {
                currentSolution = new ArrayList<>(newSolution);
            }

            if (calculateObjectiveValue(currentSolution, depot) < calculateObjectiveValue(bestSolution, depot)) {
                bestSolution = new ArrayList<>(currentSolution);
                System.out.println("New best solution found: " + calculateObjectiveValue(bestSolution, depot));
            }

            temperature *= 1 - coolingRate;
        }

        return convertToRouteLegs(bestSolution, depot, vehicleCapacity);
    }

    private List<Address> generateNeighborSolution(List<Address> currentSolution, Long vehicleCapacity) {
        List<Address> neighborSolution = new ArrayList<>(currentSolution);
        Random random = new Random();
        int strategy = random.nextInt(3); // Randomly choose a strategy (0: Swap, 1: Reversal, 2: Insertion)

        switch (strategy) {
            case 0: // Swap
                int index1 = 1 + random.nextInt(neighborSolution.size() - 1);
                int index2 = 1 + random.nextInt(neighborSolution.size() - 1);
                while (index1 == index2) {
                    index2 = 1 + random.nextInt(neighborSolution.size() - 1);
                }
                Collections.swap(neighborSolution, index1, index2);
                break;

            case 1: // Reversal
                int start = 1 + random.nextInt(neighborSolution.size() - 2);
                int end = start + random.nextInt(neighborSolution.size() - start);
                Collections.reverse(neighborSolution.subList(start, end + 1));
                break;

            case 2: // Insertion
                int removeIndex = 1 + random.nextInt(neighborSolution.size() - 1);
                Address address = neighborSolution.remove(removeIndex);
                int insertIndex = 1 + random.nextInt(neighborSolution.size() - 1);
                neighborSolution.add(insertIndex, address);
                break;
        }

        return neighborSolution;
    }

    private double calculateObjectiveValue(List<Address> solution, Address depot) {
        double totalTravelTime = 0.0;

        for (int i = 0; i < solution.size() - 1; i++) {
            TimeDistance timeDistance = getTimeDistanceBetweenAddresses(solution.get(i), solution.get(i + 1));
            totalTravelTime += timeDistance.getTime();
        }

        Address lastAddress = solution.get(solution.size() - 1);
        TimeDistance backToDepot = getTimeDistanceBetweenAddresses(lastAddress, depot);
        totalTravelTime += backToDepot.getTime();

        return totalTravelTime;
    }

    private List<RouteLeg> convertToRouteLegs(List<Address> bestSolution, Address depot, Long vehicleCapacity) {
        List<RouteLeg> routeLegs = new ArrayList<>();
        Long currentCapacity = vehicleCapacity;

        for (int i = 0; i < bestSolution.size() - 1; i++) {
            Address from = bestSolution.get(i);
            Address to = bestSolution.get(i + 1);
            Long remainingDemand = to.getUnit();

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

            if (currentCapacity == 0) {
                routeLegs.addAll(returnToDepotAndRefill(to, depot));
                currentCapacity = vehicleCapacity;

                if (i < bestSolution.size() - 2) {
                    Address nextTo = bestSolution.get(i + 2);
                    routeLegs.addAll(addLegFromDepotToNextAddress(depot, nextTo, nextTo.getUnit()));
                    i++;
                }
            }
        }

        addFinalLegToDepot(bestSolution, depot, routeLegs);

        System.out.println("Final Route on Test:");
        for (RouteLeg leg : routeLegs) {
            System.out.println("From ID: " + leg.getOriginId() + " To ID: " + leg.getDestinationId() +
                    " - Distance: " + leg.getDistance() + "m, Time: " + leg.getTime() + "s");
        }
        return routeLegs;
    }

    private TimeDistance getTimeDistanceBetweenAddresses(Address from, Address to) {
        return distanceMatrixService.getDistanceAndTime(
                from.getLatitude() + "," + from.getLongitude(),
                to.getLatitude() + "," + to.getLongitude());
    }

    private double acceptanceProbability(double currentEnergy, double newEnergy, double temperature) {
        if (newEnergy < currentEnergy) {
            return 1.0;
        }
        return Math.exp((currentEnergy - newEnergy) / temperature);
    }

    private Address findDepot(List<Address> addresses) {
        return addresses.stream()
                .filter(address -> address.getId().equals(1L))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Depot not found"));
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

    private void addFinalLegToDepot(List<Address> bestSolution, Address depot, List<RouteLeg> routeLegs) {
        Address lastAddress = bestSolution.get(bestSolution.size() - 1);
        if (!lastAddress.equals(depot)) {
            TimeDistance backToDepot = getTimeDistanceBetweenAddresses(lastAddress, depot);
            routeLegs.add(new RouteLeg(lastAddress.getId(), depot.getId(), lastAddress.getLatitude(), lastAddress.getLongitude(),
                    depot.getLatitude(), depot.getLongitude(), backToDepot.getTime(), backToDepot.getDistance(), 0L));
        }
    }

    // Nearest Neighbor part
    public List<Address> generateInitialSolutionForSA(Address depot, List<Address> addresses) {
        List<Address> routeAddresses = new ArrayList<>();
        if (addresses == null || addresses.isEmpty()) {
            return routeAddresses;
        }

        routeAddresses.add(depot);

        List<Address> tempAddresses = new ArrayList<>(addresses);
        tempAddresses.remove(depot);

        Address currentAddress = depot;

        while (!tempAddresses.isEmpty()) {
            Address nextAddress = findNearestDestinationWithoutCapacity(currentAddress, tempAddresses);
            if (nextAddress == null) {
                throw new IllegalStateException("No feasible next address found without considering capacity.");
            }

            routeAddresses.add(nextAddress);
            tempAddresses.remove(nextAddress);
            currentAddress = nextAddress;
        }

        return routeAddresses;
    }

    // Testing purposes
    public List<Address> generateInitialSolutionForSA(List<Address> addresses) {
        List<Address> routeAddresses = new ArrayList<>();
        Address depot = addresses.get(0);

        if (addresses == null || addresses.isEmpty()) {
            return routeAddresses;
        }

        routeAddresses.add(depot);

        List<Address> tempAddresses = new ArrayList<>(addresses);
        tempAddresses.remove(depot);

        Address currentAddress = depot;

        while (!tempAddresses.isEmpty()) {
            Address nextAddress = findNearestDestinationWithoutCapacity(currentAddress, tempAddresses);
            if (nextAddress == null) {
                throw new IllegalStateException("No feasible next address found without considering capacity.");
            }

            routeAddresses.add(nextAddress);
            tempAddresses.remove(nextAddress);
            currentAddress = nextAddress;
        }

        return routeAddresses;
    }

    private Address findNearestDestinationWithoutCapacity(Address origin, List<Address> potentialDestinations) {
        Address optimalDestination = null;
        Double shortestTime = Double.MAX_VALUE;
        Double shortestDistance = Double.MAX_VALUE;

        for (Address destination : potentialDestinations) {
            if (!destination.equals(origin)) {
                TimeDistance timeDistance = getTimeDistanceBetweenAddresses(origin, destination);

                if (timeDistance.getTime() < shortestTime || (Double.compare(timeDistance.getTime(), shortestTime) == 0 && timeDistance.getDistance() < shortestDistance)) {
                    shortestTime = timeDistance.getTime();
                    shortestDistance = timeDistance.getDistance();
                    optimalDestination = destination;
                }
            }
        }
        return optimalDestination;
    }

    public double getInitialTemperature() {
        return initialTemperature;
    }

    public double getCoolingRate() {
        return coolingRate;
    }
}
