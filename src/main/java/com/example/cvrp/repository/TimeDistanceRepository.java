package com.example.cvrp.repository;


import com.example.cvrp.model.TimeDistanceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TimeDistanceRepository extends JpaRepository<TimeDistanceEntity, Long> {
    Optional<TimeDistanceEntity> findByOriginAndDestination(String origin, String destination);
}
