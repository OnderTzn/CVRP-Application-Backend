package com.example.cvrp.service;

import com.example.cvrp.exceptions.ItemNotFoundException;
import com.example.cvrp.model.Address;
import com.example.cvrp.repository.AddressRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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

    public List<Address> getAddressesByIds(List<Long> addressIds) {
        // Check if the list is null or empty
        if (addressIds == null || addressIds.isEmpty()) {
            return Collections.emptyList();
        }

        // Filter out any null values from the list
        List<Long> filteredIds = addressIds.stream()
                .filter(id -> id != null)
                .collect(Collectors.toList());

        // If all IDs were null, return an empty list
        if (filteredIds.isEmpty()) {
            return Collections.emptyList();
        }

        return addressRepository.findAllById(filteredIds);
    }

    public void saveAddressesFromCsv(MultipartFile file) throws Exception {
        List<Address> addresses = new ArrayList<>();
        try (Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim());
            for (CSVRecord csvRecord : csvParser) {
                Address address = new Address();
                address.setLatitude(Double.parseDouble(csvRecord.get("LAT")));
                address.setLongitude(Double.parseDouble(csvRecord.get("LON")));
                address.setUnit(Long.parseLong(csvRecord.get("UNIT")));
                addresses.add(address);
            }
            addressRepository.saveAll(addresses);
        }
    }
}
