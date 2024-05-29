package com.example.cvrp.service;

import com.example.cvrp.model.AlgorithmResult;
import com.example.cvrp.repository.AlgorithmResultRepository;
import org.springframework.stereotype.Service;

@Service
public class AlgorithmResultService {
    private final AlgorithmResultRepository algorithmResultRepository;

    public AlgorithmResultService(AlgorithmResultRepository algorithmResultRepository) {
        this.algorithmResultRepository = algorithmResultRepository;
    }

    public AlgorithmResult saveResult(AlgorithmResult result) {
        return algorithmResultRepository.save(result);
    }
}
