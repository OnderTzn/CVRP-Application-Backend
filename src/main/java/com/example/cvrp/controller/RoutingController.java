package com.example.cvrp.controller;

import com.example.cvrp.dto.RouteLeg;
import com.example.cvrp.model.Address;
import com.example.cvrp.service.AddressService;
import com.example.cvrp.service.AddressServiceImp;
import com.example.cvrp.dto.RouteRequest;
import com.example.cvrp.service.RoutingServiceImp;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin("http://localhost:3000")
@AllArgsConstructor
@RequestMapping("/routing") // /address
public class RoutingController {

    private AddressService addressService;
    private AddressServiceImp addressServiceImp;
    private RoutingServiceImp routingServiceImp;

    @PostMapping("/calculate-route")
    public ResponseEntity<?> calculateRoute(@RequestBody RouteRequest routeRequest) {
        System.out.println("Algorithm Type: " + routeRequest.getAlgorithm());
        System.out.println("Depot ID: " + routeRequest.getDepotId());
        System.out.println("Address IDs: " + routeRequest.getAddressList());
        System.out.println("Vehicle Capacity: " + routeRequest.getCapacity());

        Address depot = addressService.findAddressById(routeRequest.getDepotId());
        System.out.println("Depot found: " + depot);

        List<Address> addresses = addressServiceImp.getAddressesByIds(routeRequest.getAddressList());
        System.out.println("Number of addresses fetched: " + addresses.size());
        addresses.forEach(address -> System.out.println("Fetched address ID: " + address.getId()));

        List<RouteLeg> routeLegs = routingServiceImp.calculateRoute(routeRequest.getAlgorithm(), depot, addresses, routeRequest.getCapacity());
        System.out.println("Number of route legs calculated: " + routeLegs.size());
        return ResponseEntity.ok(routeLegs);
    }

    @GetMapping("/calculateRoute/{algorithmType}/{addressLimit}/{vehicleCapacity}")
    public ResponseEntity<List<RouteLeg>> calculateRoute(
            @PathVariable String algorithmType,
            @PathVariable int addressLimit,
            @PathVariable Long vehicleCapacity) {

        List<RouteLeg> route = routingServiceImp.calculateOptimalRoute(algorithmType, addressLimit, vehicleCapacity);
        return ResponseEntity.ok(route);
    }
}
