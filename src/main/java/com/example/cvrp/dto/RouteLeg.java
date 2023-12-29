package com.example.cvrp.dto;

import lombok.Data;

@Data
public class RouteLeg {
    private Long destinationId;
    private Double latitude;
    private Double longitude;
    private Double time;
    private Double distance;

    public RouteLeg(Long destinationId, Double latitude, Double longitude, Double time, Double distance) {
        this.destinationId = destinationId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.time = time;
        this.distance = distance;
    }

    public RouteLeg(Long destinationId, Double latitude, Double longitude) {
        this.destinationId = destinationId;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
