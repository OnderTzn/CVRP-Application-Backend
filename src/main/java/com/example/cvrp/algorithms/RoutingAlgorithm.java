package com.example.cvrp.algorithms;

import com.example.cvrp.model.Address;
import com.example.cvrp.dto.RouteLeg;

import java.util.List;

public interface RoutingAlgorithm {

    List<RouteLeg> calculateRoute(List<Address> addresses, Long vehicleCapacity);

}
