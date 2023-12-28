package com.example.cvrp.service;

import com.example.cvrp.model.Address;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface AddressService {

    Address addAddress(Address address);

    void deleteAddress(Long id);

    void updateAddress(Long addressId, Address address);

    Address findAddressById(Long id);

    List<Address> findAllAddresses();

    List<Address> findAllAddresses(int addressLimit);
}
