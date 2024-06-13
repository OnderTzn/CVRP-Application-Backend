package com.example.cvrp.serviceTest;

import com.example.cvrp.dto.RouteCalculationResult;
import com.example.cvrp.service.RoutingServiceImp;
import com.example.cvrp.util.MemoryUsageUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class RoutingTestServiceImp {

    private final RoutingServiceImp routingServiceImp;

    private static final Logger log = LoggerFactory.getLogger(RoutingTestServiceImp.class);

    @Autowired
    public RoutingTestServiceImp(RoutingServiceImp routingServiceImp) {
        this.routingServiceImp = routingServiceImp;
    }

    public void runTests() {
        log.info("Starting the test process...");

        // Define the specific combinations of addressCounts and capacities
        int[] addressCounts = {15};
        long[] capacities = {150L}; // Make sure the lengths of addressCounts and capacities are equal

        String[] algorithms = {"SimulatedAnnealingTest"}; // NearestNeighborTest   // SavingsTest   // SimulatedAnnealingTest   // NearestNeighborSATest   // AlgorithmName

        for (String algorithm : algorithms) {
            for (int i = 0; i < addressCounts.length; i++) { // Assumes capacities.length == addressCounts.length
                int addressCount = addressCounts[i];
                long capacity = capacities[i];

                MemoryUsageUtil.forceGarbageCollection();
                long memoryBefore = MemoryUsageUtil.getUsedMemory();

                // Start measuring memory usage
                //Runtime runtime = Runtime.getRuntime();
                //long beforeUsedMem = runtime.totalMemory() - runtime.freeMemory();

                // Assuming calculateOptimalRoute returns void or some result type you can handle
                long startTime = System.currentTimeMillis();
                RouteCalculationResult result = routingServiceImp.calculateRouteTest(algorithm, addressCount, capacity);
                long endTime = System.currentTimeMillis();

                // End measuring memory usage
                //long afterUsedMem = runtime.totalMemory() - runtime.freeMemory();
                //long actualMemUsed = afterUsedMem - beforeUsedMem; // Actual memory used in bytes

                MemoryUsageUtil.forceGarbageCollection();
                long memoryAfter = MemoryUsageUtil.getUsedMemory();
                long memoryUsed = memoryAfter - memoryBefore;
                //System.out.println("Memory used TEST: " + memoryUsed + " bytes");

                // Log the results
                //log.info("Algorithm: {}, Addresses: {}, Capacity: {}, Execution Time: {}ms, Route Time: {} s, Route Distance: {} m, Returns to Depot: {}",
                //        algorithm, addressCount, capacity, (endTime - startTime), String.format("%.2f", result.getTotalTime()), String.format("%.2f", result.getTotalDistance()), result.getReturnsToDepot());

                log.info("Algorithm: {}, Addresses: {}, Capacity: {}, Route Time: {} s, Route Distance: {} m, Execution Time: {}ms, Returns to Depot: {}, Memory Usage: {} bytes",
                        algorithm, addressCount, capacity, String.format("%.2f", result.getTotalTime()), String.format("%.2f", result.getTotalDistance()), (endTime - startTime), result.getReturnsToDepot(), memoryUsed);

            }
        }
        // curl http://localhost:8080/api/tests/run
        log.info("Test process completed.");
    }

}

