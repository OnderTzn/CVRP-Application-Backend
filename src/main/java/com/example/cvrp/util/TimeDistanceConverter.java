package com.example.cvrp.util;

import com.example.cvrp.dto.TimeDistance;
import com.example.cvrp.model.TimeDistanceEntity;

public class TimeDistanceConverter {

    public static TimeDistanceEntity toEntity(TimeDistance dto) {
        return new TimeDistanceEntity(dto.getOrigin(), dto.getDestination(), dto.getTime(), dto.getDistance());
    }

    public static TimeDistance toDto(TimeDistanceEntity entity) {
        return new TimeDistance(entity.getOrigin(), entity.getDestination(), entity.getTime(), entity.getDistance());
    }
}
