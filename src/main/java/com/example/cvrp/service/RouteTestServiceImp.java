package com.example.cvrp.service;

import com.example.cvrp.dto.RouteCalculationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class RouteTestServiceImp {

    private final DistanceServiceImp distanceServiceImp;

    private static final Logger log = LoggerFactory.getLogger(RouteTestServiceImp.class);

    @Autowired
    public RouteTestServiceImp(DistanceServiceImp distanceServiceImp) {
        this.distanceServiceImp = distanceServiceImp;
    }

    public void runTests() {
        log.info("Starting the test process...");

        // Define the specific combinations of addressCounts and capacities
        int[] addressCounts = {40};
        long[] capacities = {50L}; // Make sure the lengths of addressCounts and capacities are equal

        String[] algorithms = {"NearestNeighbor", "Savings"};

        for (String algorithm : algorithms) {
            for (int i = 0; i < addressCounts.length; i++) { // Assumes capacities.length == addressCounts.length
                int addressCount = addressCounts[i];
                long capacity = capacities[i];

                // Assuming calculateOptimalRoute returns void or some result type you can handle
                long startTime = System.currentTimeMillis();
                RouteCalculationResult result = distanceServiceImp.calculateRouteAndGetDetails(algorithm, addressCount, capacity);
                long endTime = System.currentTimeMillis();

                // Log the results
                log.info("Algorithm: {}, Addresses: {}, Capacity: {}, Execution Time: {}ms, Route Time: {} s, Route Distance: {} m, Returns to Depot: {}",
                        algorithm, addressCount, capacity, (endTime - startTime), String.format("%.2f", result.getTotalTime()), String.format("%.2f", result.getTotalDistance()), result.getReturnsToDepot());
            }
        }

        log.info("Test process completed.");
    }

}

