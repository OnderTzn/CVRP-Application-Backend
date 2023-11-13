package com.example.cvrp.repository;

import com.example.cvrp.model.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {

    Address findAddressById(Long addressId);

    default boolean isAddressExist(Long addressId) {
        return findAddressById(addressId) != null;
    }

}
