package com.example.cvrp.dto;

import java.util.List;

public class RouteCalculationResult {
    private final List<RouteLeg> route;
    private final long executionTime;
    private final double totalDistance;
    private final double totalTime;
    private final int returnsToDepot;

    // Constructor
    public RouteCalculationResult(List<RouteLeg> route, long executionTime, double totalDistance, double totalTime, int returnsToDepot) {
        this.route = route;
        this.executionTime = executionTime;
        this.totalDistance = totalDistance;
        this.totalTime = totalTime;
        this.returnsToDepot = returnsToDepot;
    }

    // Getters
    public List<RouteLeg> getRoute() { return route; }
    public long getExecutionTime() { return executionTime; }
    public double getTotalDistance() { return totalDistance; }
    public double getTotalTime() { return totalTime; }
    public int getReturnsToDepot() { return returnsToDepot; }
}
