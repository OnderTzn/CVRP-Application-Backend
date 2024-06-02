package com.example.cvrp.service;

import com.example.cvrp.model.Address;
import com.example.cvrp.model.GoogleMapsResponse;
import com.example.cvrp.dto.TimeDistance;
import com.example.cvrp.model.TimeDistanceEntity;
import com.example.cvrp.repository.TimeDistanceRepository;
import com.example.cvrp.util.TimeDistanceConverter;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DistanceMatrixServiceImp {
    private final GoogleMapsServiceImp googleMapsService;
    private final TimeDistanceRepository timeDistanceRepository;
    private int googleMapsRequestCount = 0; // Counter for Google Maps API requests

    public DistanceMatrixServiceImp(GoogleMapsServiceImp googleMapsService, TimeDistanceRepository timeDistanceRepository) {
        this.googleMapsService = googleMapsService;
        this.timeDistanceRepository = timeDistanceRepository;
    }

    public TimeDistance getDistanceAndTime(String origin, String destination) {
        // Check if the data is already in the database
        Optional<TimeDistanceEntity> existingEntry = timeDistanceRepository.findByOriginAndDestination(origin, destination);
        if (existingEntry.isPresent()) {
            return TimeDistanceConverter.toDto(existingEntry.get());
        }
        else {
            throw new RuntimeException("Origin: " + origin + ", Destination: " + destination + " doesn't exist in database");
        }

        /*
        // Fetch from Google Maps API if not in database
        GoogleMapsResponse response = googleMapsService.getDistanceMatrix(origin, destination);
        googleMapsRequestCount++; // Increment the counter

        if (response != null && !response.getRows().isEmpty() && !response.getRows().get(0).getElements().isEmpty()) {
            double time = response.getRows().get(0).getElements().get(0).getDuration().getValue();
            double distance = response.getRows().get(0).getElements().get(0).getDistance().getValue();
            TimeDistance timeDistance = new TimeDistance(origin, destination, time, distance);

            // Save to database
            timeDistanceRepository.save(TimeDistanceConverter.toEntity(timeDistance));

            return timeDistance;
        } else {
            throw new RuntimeException("Failed to get distance and time from Google Maps API");
        }*/
    }
    //ADDED FOR TESTING
    public void fetchAndSaveAllTimeDistances(List<Address> addresses) {
        for (int i = 0; i < addresses.size(); i++) {
            for (int j = i + 1; j < addresses.size(); j++) {
                Address from = addresses.get(i);
                Address to = addresses.get(j);
                if (!from.equals(to)) { // Ensure from and to addresses are different
                    String origin = from.getLatitude() + "," + from.getLongitude();
                    String destination = to.getLatitude() + "," + to.getLongitude();

                    // Check if data exists in the database
                    Optional<TimeDistanceEntity> existingEntry = timeDistanceRepository.findByOriginAndDestination(origin, destination);
                    if (existingEntry.isEmpty()) {
                        // Fetch from Google Maps API if not in database and save it
                        getDistanceAndTime(origin, destination);
                    }
                }
            }
        }
        System.out.println("First step completed. Google Maps API requests count: " + googleMapsRequestCount);
        for (int i = addresses.size() - 1; i >= 0; i--) {
            for (int j = addresses.size() - 1; j > i; j--) {
                Address from = addresses.get(i);
                Address to = addresses.get(j);
                if (!from.equals(to)) { // Ensure from and to addresses are different
                    String origin = from.getLatitude() + "," + from.getLongitude();
                    String destination = to.getLatitude() + "," + to.getLongitude();

                    // Check if data exists in the database
                    Optional<TimeDistanceEntity> existingEntry = timeDistanceRepository.findByOriginAndDestination(origin, destination);
                    if (existingEntry.isEmpty()) {
                        // Fetch from Google Maps API if not in database and save it
                        getDistanceAndTime(origin, destination);
                    }
                }
            }
        }
        // Print the Google Maps API request count
        System.out.println("Completed. Google Maps API requests count: " + googleMapsRequestCount);
        googleMapsRequestCount = 0;
    }

    public int getGoogleMapsRequestCount() {
        return googleMapsRequestCount;
    }
}
