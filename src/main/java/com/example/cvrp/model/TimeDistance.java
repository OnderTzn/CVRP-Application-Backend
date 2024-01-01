package com.example.cvrp.model;

public class TimeDistance {
    private double time; // Time in some units (e.g., seconds)
    private double distance; // Distance in some units (e.g., meters)

    // Constructor
    public TimeDistance(double time, double distance) {
        this.time = time;
        this.distance = distance;
    }

    // Getters
    public double getTime() {
        return time;
    }

    public double getDistance() {
        return distance;
    }

    // Method to compare two TimeDistance objects
    public boolean isBetterThan(TimeDistance other) {
        if (this.time < other.time) {
            return true;
        }
        else return this.time == other.time && this.distance < other.distance;
    }

    // Method to add two TimeDistance objects
    public TimeDistance add(TimeDistance other) {
        return new TimeDistance(this.time + other.time, this.distance + other.distance);
    }

    // toString method for debugging
    @Override
    public String toString() {
        return "TimeDistance{" +
                "time=" + time +
                ", distance=" + distance +
                '}';
    }
}

