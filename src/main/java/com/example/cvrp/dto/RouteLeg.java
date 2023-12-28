package com.example.cvrp.dto;

public class RouteLeg {
    private Long destinationId;
    private Double time;
    private Double distance;

    public RouteLeg(Long destinationId, Double time, Double distance) {
        this.destinationId = destinationId;
        this.time = time;
        this.distance = distance;
    }

    // Getter and setter for destinationId
    public Long getDestinationId() {
        return destinationId;
    }

    public void setDestinationId(Long destinationId) {
        this.destinationId = destinationId;
    }

    // Getter and setter for time
    public Double getTime() {
        return time;
    }

    public void setTime(Double time) {
        this.time = time;
    }

    // Getter and setter for distance
    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }
}
