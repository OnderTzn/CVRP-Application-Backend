package com.example.cvrp.model;

import com.example.cvrp.repository.AddressRepository;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RouteRequest {

    @JsonProperty("depot")
    private Long depotId;

    @JsonProperty("addressList")
    private List<Long> addressList;

    @JsonProperty("vehicleCapacity")
    private long capacity;

}
