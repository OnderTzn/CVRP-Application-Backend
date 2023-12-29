package com.example.cvrp.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;

@Data
@Entity
@Table
@JsonSerialize
public class Address implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //Auto increment the id
    @Column(updatable = false)
    private Long id;

    @Column
    private double latitude;

    @Column
    private double longitude;

    @Column
    private Long unit;

    public void updateAddress(Address address) {
        this.latitude = address.latitude;
        this.longitude = address.longitude;
    }


    public String toString() {
        return "Address{" +
                "id=" + id +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", unit=" + unit +
                '}';
    }
}
