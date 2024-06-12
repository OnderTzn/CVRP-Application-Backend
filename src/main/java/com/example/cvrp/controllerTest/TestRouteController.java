package com.example.cvrp.controllerTest;

import com.example.cvrp.serviceTest.AddressPreparationService;
import com.example.cvrp.service.RoutingServiceImp;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class TestRouteController {

    private final AddressPreparationService addressPreparationService;
    private final RoutingServiceImp routingService;

    public TestRouteController(AddressPreparationService addressPreparationService, RoutingServiceImp routingService) {
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
