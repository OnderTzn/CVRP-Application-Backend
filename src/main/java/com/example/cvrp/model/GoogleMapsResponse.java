package com.example.cvrp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GoogleMapsResponse {
    // Assuming a simplified structure for example purposes
    private List<Row> rows;

    // Standard getters and setters
    public List<Row> getRows() {
        return rows;
    }

    public void setRows(List<Row> rows) {
        this.rows = rows;
    }

    // Nested classes to match the API response structure
    public static class Row {
        private List<Element> elements;

        // Standard getters and setters
        public List<Element> getElements() {
            return elements;
        }

        public void setElements(List<Element> elements) {
            this.elements = elements;
        }
    }

    public static class Element {
        private Distance distance;
        private Duration duration;

        // Standard getters and setters
        public Distance getDistance() {
            return distance;
        }

        public void setDistance(Distance distance) {
            this.distance = distance;
        }

        public Duration getDuration() {
            return duration;
        }

        public void setDuration(Duration duration) {
            this.duration = duration;
        }
    }

    public static class Distance {
        private String text;
        private long value;

        // Standard getters and setters
    }

    public static class Duration {
        private String text;
        private long value;

        // Standard getters and setters
    }
}
