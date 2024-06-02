package com.example.cvrp.algorithms;

import com.example.cvrp.dto.RouteLeg;
import com.example.cvrp.model.Address;
import com.example.cvrp.dto.TimeDistance;
import com.example.cvrp.service.DistanceMatrixServiceImp;

import java.util.*;
import java.util.stream.Collectors;

public class SimulatedAnnealingAlgorithmTest implements RoutingAlgorithm {
    private final DistanceMatrixServiceImp distanceMatrixService;
    private final double initialTemperature = 10000;
    private double temperature = initialTemperature;
    private double coolingRate = 0.01;

    public SimulatedAnnealingAlgorithmTest(DistanceMatrixServiceImp distanceMatrixService) {
        this.distanceMatrixService = distanceMatrixService;
    }

    @Override
    public List<RouteLeg> calculateRoute(Address depot, List<Address> addresses, long vehicleCapacity) {
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
        List<Address> currentSolution = generateInitialSolution(addresses, depot);
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
                System.out.println("New best solution found: " + calculateObjectiveValue(bestSolution, depot));
            }

            temperature *= 1 - coolingRate;
        }

        return convertToRouteLegs(bestSolution, depot, vehicleCapacity);
    }

    //For test purposes
    public List<RouteLeg> calculateRoute(List<Address> addresses, Long vehicleCapacity) {
        System.out.println("ON THE TEST FUNCTION");
        System.out.println("Simulated Annealing TEST Algorithm");
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
        List<Address> addressesWithoutDepot = addresses.stream()
                .filter(address -> !address.equals(depot))
                .collect(Collectors.toList());
        List<Address> currentSolution = generateInitialSolution(addressesWithoutDepot, depot);
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

    public List<Address> generateInitialSolution(List<Address> addresses, Address depot) {
        List<Address> initialSolution = new ArrayList<>();
        initialSolution.add(depot);
        List<Address> shuffledAddresses = new ArrayList<>(addresses);
        Collections.shuffle(shuffledAddresses);
        initialSolution.addAll(shuffledAddresses);
        return initialSolution;
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

        System.out.println("Final Route on test:");
        for (RouteLeg leg : routeLegs) {
            System.out.println("From ID: " + leg.getOriginId() + " To ID: " + leg.getDestinationId() +
                    " - Distance: " + leg.getDistance() + "m, Time: " + leg.getTime() + "s, Capacity Used: " + leg.getVehicleCapacity() + " units");
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
        double probability = Math.exp((currentEnergy - newEnergy) / temperature);
        //System.out.println("Acceptance Probability: " + probability + " (Current Energy: " + currentEnergy + ", New Energy: " + newEnergy + ", Temperature: " + temperature + ")");
        return probability;
    }

    private Address findDepot(List<Address> addresses) {
        return addresses.stream()
                .filter(address -> address.getId().equals(1L)) // Assuming depot's ID is 1
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

    public double getInitialTemperature() {
        return initialTemperature;
    }

    public double getCoolingRate() {
        return coolingRate;
    }
}
