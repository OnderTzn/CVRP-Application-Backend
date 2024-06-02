package com.example.cvrp.controller;

import com.example.cvrp.dto.RouteCalculationResult;
import com.example.cvrp.dto.RouteRequest;
import com.example.cvrp.service.AddressPreparationService;
import com.example.cvrp.service.RoutingServiceImp;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class RouteController {

    private final AddressPreparationService addressPreparationService;
    private final RoutingServiceImp routingService;

    public RouteController(AddressPreparationService addressPreparationService, RoutingServiceImp routingService) {
        this.addressPreparationService = addressPreparationService;
        this.routingService = routingService;
    }

    @GetMapping("/prepare-addresses/{addressLimit}")
    public void prepareAddresses(@PathVariable int addressLimit) {
        System.out.println("Fetching started for " + addressLimit + " addresses.");
        addressPreparationService.prepareAddressesForCalculation(addressLimit);
        System.out.println("Fetching ended for " + addressLimit + " addresses.");
    }

}