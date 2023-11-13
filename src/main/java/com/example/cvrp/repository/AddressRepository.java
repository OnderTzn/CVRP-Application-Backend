package com.example.cvrp.repository;

import com.example.cvrp.model.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {

    @Override
    List<Address> findAll();

    Address findAddressById(Long addressId);

    default boolean isAddressExist(Long addressId) {
        return findAddressById(addressId) != null;
    }

}
