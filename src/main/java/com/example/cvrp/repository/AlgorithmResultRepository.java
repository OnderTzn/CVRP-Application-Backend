package com.example.cvrp.repository;

import com.example.cvrp.model.AlgorithmResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlgorithmResultRepository extends JpaRepository<AlgorithmResult, Long> {
}
