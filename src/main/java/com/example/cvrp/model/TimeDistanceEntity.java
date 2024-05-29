package com.example.cvrp.model;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class TimeDistanceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String origin;
    private String destination;
    private double time;
    private double distance;

    // Default constructor for JPA
    public TimeDistanceEntity() {}

    // Custom constructor
    public TimeDistanceEntity(String origin, String destination, double time, double distance) {
        this.origin = origin;
        this.destination = destination;
        this.time = time;
        this.distance = distance;
    }
}
