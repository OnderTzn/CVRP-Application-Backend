package com.example.cvrp.repository;

import com.example.cvrp.model.RouteLegEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RouteLegRepository extends JpaRepository<RouteLegEntity, Long> {
}