package com.example.cvrp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RouteRequest {

    @JsonProperty("algorithm")
    private String algorithm;

    @JsonProperty("depot")
    private Long depotId;

    @JsonProperty("addressList")
    private List<Long> addressList;

    @JsonProperty("vehicleCapacity")
    private long capacity;

}
