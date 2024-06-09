package com.example.cvrp.model;


import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "algorithm_result")
public class AlgorithmResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String algorithmType;
    private int addressCount;
    private Long vehicleCapacity;
    private Double initialTemperature;
    private Double coolingRate;
    private double totalTime;
    private double totalDistance;
    private long executionTime;
    private int returnsToDepot;
    private long memoryUsage;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "algorithmResult")
    @JsonManagedReference
    private List<RouteLegEntity> routeLegs;

    // Constructors, getters, and setters

    public AlgorithmResult() {}

    public AlgorithmResult(String algorithmType, int addressCount, Long vehicleCapacity, Double initialTemperature,
                           Double coolingRate, double totalTime, double totalDistance, long executionTime,
                           long memoryUsage, int returnsToDepot) {
        this.algorithmType = algorithmType;
        this.addressCount = addressCount;
        this.vehicleCapacity = vehicleCapacity;
        this.initialTemperature = initialTemperature;
        this.coolingRate = coolingRate;
        this.totalTime = totalTime;
        this.totalDistance = totalDistance;
        this.executionTime = executionTime;
        this.memoryUsage = memoryUsage;
        this.returnsToDepot = returnsToDepot;
    }

    public void setRouteLegs(List<RouteLegEntity> routeLegs) {
        this.routeLegs = routeLegs;
        for (RouteLegEntity routeLeg : routeLegs) {
            routeLeg.setAlgorithmResult(this);
        }
    }


}

