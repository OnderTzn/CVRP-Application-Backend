package com.example.cvrp.dto;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
public class TimeDistance {
    // Getters and setters
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String origin;
    private String destination;
    private double time;
    private double distance;

    // Default constructor required by JPA
    public TimeDistance() {
    }

    // Constructor
    public TimeDistance(String origin, String destination, double time, double distance) {
        this.origin = origin;
        this.destination = destination;
        this.time = time;
        this.distance = distance;
    }

    public TimeDistance(double time, double distance) {
        this.time = time;
        this.distance = distance;
    }

    // Method to add two TimeDistance objects
    public TimeDistance add(TimeDistance other) {
        return new TimeDistance(this.origin, this.destination, this.time + other.time, this.distance + other.distance);
    }

    // toString method for debugging
    @Override
    public String toString() {
        return "TimeDistance{" +
                "id=" + id +
                ", origin='" + origin + '\'' +
                ", destination='" + destination + '\'' +
                ", time=" + time +
                ", distance=" + distance +
                '}';
    }
}
