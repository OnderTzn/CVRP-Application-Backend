package com.example.cvrp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GoogleMapsResponse {

    private List<Row> rows;

    //Getters and setters
    public List<Row> getRows() {
        return rows;
    }

    public void setRows(List<Row> rows) {
        this.rows = rows;
    }

    // Nested classes to match the API response structure
    public static class Row {
        private List<Element> elements;

        //Getters and setters
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

        //Getters and setters
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

    @Getter
    @Setter
    public static class Distance {
        private String text;
        private Double value;
    }

    @Getter
    @Setter
    public static class Duration {
        private String text;
        private Double value;
    }
}
