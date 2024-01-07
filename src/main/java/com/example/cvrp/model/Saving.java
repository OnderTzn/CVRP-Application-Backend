package com.example.cvrp.model;

public class Saving {
    private Address address1;
    private Address address2;
    private double saving;

    public Saving(Address address1, Address address2, double saving) {
        this.address1 = address1;
        this.address2 = address2;
        this.saving = saving;
    }

    // Getters and setters
    public Address getAddress1() {
        return address1;
    }

    public void setAddress1(Address address1) {
        this.address1 = address1;
    }

    public Address getAddress2() {
        return address2;
    }

    public void setAddress2(Address address2) {
        this.address2 = address2;
    }

    public double getSaving() {
        return saving;
    }

    public void setSaving(double saving) {
        this.saving = saving;
    }
}
