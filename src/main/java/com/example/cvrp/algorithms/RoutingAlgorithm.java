package com.example.cvrp.algorithms;

import com.example.cvrp.dto.RouteLeg;
import com.example.cvrp.model.Address;
import java.util.List;

public interface RoutingAlgorithm {

    List<RouteLeg> calculateRoute(List<Address> addresses, Long vehicleCapacity);

}