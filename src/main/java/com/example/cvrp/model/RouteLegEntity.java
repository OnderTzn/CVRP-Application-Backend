package com.example.cvrp.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "route_leg")
public class RouteLegEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "origin_id")
    private Long originId;

    @Column(name = "destination_id")
    private Long destinationId;

    @Column(name = "origin_latitude")
    private double originLatitude;

    @Column(name = "origin_longitude")
    private double originLongitude;

    @Column(name = "destination_latitude")
    private double destinationLatitude;

    @Column(name = "destination_longitude")
    private double destinationLongitude;

    @Column(name = "time")
    private double time;

    @Column(name = "distance")
    private double distance;

    @Column(name = "vehicle_capacity")
    private Long vehicleCapacity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "algorithm_result_id", nullable = false)
    private AlgorithmResult algorithmResult;

    public RouteLegEntity(Long originId, Long destinationId, double originLatitude, double originLongitude,
                          double destinationLatitude, double destinationLongitude, double time,
                          double distance, Long vehicleCapacity) {
        this.originId = originId;
        this.destinationId = destinationId;
        this.originLatitude = originLatitude;
        this.originLongitude = originLongitude;
        this.destinationLatitude = destinationLatitude;
        this.destinationLongitude = destinationLongitude;
        this.time = time;
        this.distance = distance;
        this.vehicleCapacity = vehicleCapacity;
    }

    // Default constructor for JPA
    public RouteLegEntity() {
    }


}
