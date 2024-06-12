package com.example.cvrp.serviceTest;

import com.example.cvrp.model.Address;
import com.example.cvrp.service.AddressService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AddressPreparationService {
    private final AddressService addressService;
    private final DistanceMatrixServiceImp distanceMatrixService;

    public AddressPreparationService(AddressService addressService, DistanceMatrixServiceImp distanceMatrixService) {
        this.addressService = addressService;
        this.distanceMatrixService = distanceMatrixService;
    }

    public void prepareAddressesForCalculation(int addressLimit) {
        // Fetch addresses
        List<Address> addresses = addressService.findAllAddresses(addressLimit + 1);

        // Fetch and save all time-distance data
        distanceMatrixService.fetchAndSaveAllTimeDistances(addresses);
    }
}
