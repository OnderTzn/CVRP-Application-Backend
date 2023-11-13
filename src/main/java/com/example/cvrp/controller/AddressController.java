package com.example.cvrp.controller;

import com.example.cvrp.model.Address;
import com.example.cvrp.repository.AddressRepository;
import com.example.cvrp.service.AddressServiceImp;
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
    private final AddressRepository addressRepository;

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

}
