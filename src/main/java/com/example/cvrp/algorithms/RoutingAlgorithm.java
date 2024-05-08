package com.example.cvrp.algorithms;

import com.example.cvrp.dto.RouteLeg;
import com.example.cvrp.model.Address;
import java.util.List;

public interface RoutingAlgorithm {

    List<RouteLeg> calculateRouteWithDepot(Address depot, List<Address> addresses, long vehicleCapacity);

    List<RouteLeg> calculateRoute(List<Address> addresses, Long vehicleCapacity);


    //List<RouteLeg> calculateRoute(Address depot, List<Address> addresses, long vehicleCapacity);

}