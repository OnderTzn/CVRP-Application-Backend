package com.example.cvrp.dto;

import com.example.cvrp.model.Address;
import lombok.Data;

@Data
public class RouteLeg {
    private Long originId;
    private Long destinationId;
    private Double latitude;
    private Double longitude;
    private Double destLatitude;
    private Double destLongitude;
    private Double time;
    private Double distance;
    private Long vehicleCapacity;

    public RouteLeg(Long destinationId, Double latitude, Double longitude, Double time, Double distance) {
        this.destinationId = destinationId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.time = time;
        this.distance = distance;
    }

    public RouteLeg(Long id, double latitude, double longitude) {
        this.destinationId = id;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public RouteLeg(Long originId, Long destinationId, Double orgLatitude, Double orgLongitude, Double destLatitude, Double destLongitude, Double time, Double distance) {
        this.originId = originId;
        this.destinationId = destinationId;
        this.latitude = orgLatitude;
        this.longitude = orgLongitude;
        this.destLatitude = destLatitude;
        this.destLongitude = destLongitude;
        this.time = time;
        this.distance = distance;
    }

    public RouteLeg(Long originId, Long destinationId, Double latitude, Double longitude, Double time, Double distance) {
        this.originId = originId;
        this.destinationId = destinationId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.time = time;
        this.distance = distance;
    }

    public RouteLeg(Long originId, Long destinationId, Double distance, Double time, Long vehicleCapacity) {
        this.originId = originId;
        this.destinationId = destinationId;
        this.distance = distance;
        this.time = time;
        this.vehicleCapacity = vehicleCapacity;
    }

    public RouteLeg(Long originId, Long destinationId, Double originLatitude, Double originLongitude,
                    Double destinationLatitude, Double destinationLongitude, Double time, Double distance, Long capacity) {
        this.originId = originId;
        this.destinationId = destinationId;
        this.latitude = originLatitude;
        this.longitude = originLongitude;
        this.destLatitude = destinationLatitude;
        this.destLongitude = destinationLongitude;
        this.time = time;
        this.distance = distance;
        this.vehicleCapacity = capacity; // Set capacity
    }

}
