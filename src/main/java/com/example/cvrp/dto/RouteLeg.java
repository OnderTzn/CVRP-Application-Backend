package com.example.cvrp.dto;

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

}
