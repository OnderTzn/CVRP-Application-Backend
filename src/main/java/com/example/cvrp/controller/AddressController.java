package com.example.cvrp.controller;

import com.example.cvrp.dto.RouteLeg;
import com.example.cvrp.model.Address;
import com.example.cvrp.dto.RouteRequest;
import com.example.cvrp.service.AddressService;
import com.example.cvrp.service.AddressServiceImp;
import com.example.cvrp.service.DistanceServiceImp;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin("http://localhost:3000")
@AllArgsConstructor
@RequestMapping("/address")
public class AddressController {

    private final AddressServiceImp addressServiceImp;
    private final DistanceServiceImp distanceService;
    private AddressService addressService;

    @GetMapping("/all")
    public ResponseEntity<List<Address>> getAllAddresses() {
        List<Address> addressesList = addressServiceImp.findAllAddresses();
        return new ResponseEntity<>(addressesList, HttpStatus.OK);
    }

    //Get address by id
    @GetMapping("/find/id/{id}")
    public ResponseEntity<Address> getAddressById(@PathVariable("id") Long addressId) {
        Address address = addressServiceImp.findAddressById(addressId);
        return new ResponseEntity<>(address, HttpStatus.OK);
    }

    //Add an address
    @PostMapping("/add")
    public ResponseEntity<Address> addAddress(@RequestBody Address address) {
        Address addAddress = addressServiceImp.addAddress(address);
        return new ResponseEntity<>(addAddress, HttpStatus.CREATED);
    }

    //Update an address
    @PutMapping("/update/{addressId}")
    public ResponseEntity<Address> updateAddress(@PathVariable Long addressId, @RequestBody Address address) {
        addressServiceImp.updateAddress(addressId, address);
        return new ResponseEntity<>(address, HttpStatus.OK);
    }

    //Delete an address
    @DeleteMapping("/delete/{addressId}")
    public ResponseEntity<?> deleteAddress(@PathVariable("addressId") Long addressId) {
        addressServiceImp.deleteAddress(addressId);
        return new ResponseEntity<>(HttpStatus.OK);
    }


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

        List<RouteLeg> routeLegs = distanceService.calculateRoute(routeRequest.getAlgorithm(), depot, addresses, routeRequest.getCapacity());
        System.out.println("Number of route legs calculated: " + routeLegs.size());
        return ResponseEntity.ok(routeLegs);
    }


    @GetMapping("/calculateRoute/{algorithmType}/{addressLimit}/{vehicleCapacity}")
    public ResponseEntity<List<RouteLeg>> calculateRoute(
            @PathVariable String algorithmType,
            @PathVariable int addressLimit,
            @PathVariable Long vehicleCapacity) {

        List<RouteLeg> route = distanceService.calculateOptimalRoute(algorithmType, addressLimit, vehicleCapacity);
        return ResponseEntity.ok(route);
    }
}
