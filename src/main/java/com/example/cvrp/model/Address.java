package com.example.cvrp.model;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class Address implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //Auto increment the id
    @Column(updatable = false)
    private Long id;

    @Column
    private double longitude;

    @Column
    private double latitude;


    public void updateAddress(Address address) {
        this.longitude = address.longitude;
        this.latitude = address.latitude;
    }


    public String toString() {
        return "Address{" +
                "id=" + id +
                ", longitude=" + longitude +
                ", latitude=" + latitude +
                '}';
    }



}
