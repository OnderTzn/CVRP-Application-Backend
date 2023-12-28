package com.example.cvrp.service;

import com.example.cvrp.exceptions.ItemNotFoundException;
import com.example.cvrp.model.Address;
import com.example.cvrp.repository.AddressRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
@AllArgsConstructor
public class AddressServiceImp implements AddressService {

    private final AddressRepository addressRepository;

    //Add address
    public Address addAddress(Address address) {
        return addressRepository.save(address);
    }

    //Delete address
    public void deleteAddress(Long id) {
        addressRepository.deleteById(id);
    }

    //Update content
    public void updateAddress(Long addressId, Address address) {
        for (Address a : addressRepository.findAll()) {
            if (addressId.equals(a.getId())) {
                a.updateAddress(address);
                return;
            }
        }
        throw new ItemNotFoundException("Address not found with this id.");
    }

    //Find address by id
    public Address findAddressById(Long id) {
        return addressRepository.findById(id).orElseThrow(() -> new ItemNotFoundException("Address by id:" + id + " was not found"));
    }

    //Find all addresses
    public List<Address> findAllAddresses(){
        return addressRepository.findAll();
    }

    public List<Address> findAllAddresses(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return addressRepository.findAll(pageable).getContent();
    }
}
