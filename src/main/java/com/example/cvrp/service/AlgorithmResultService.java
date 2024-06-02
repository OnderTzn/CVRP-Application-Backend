package com.example.cvrp.service;

import com.example.cvrp.model.AlgorithmResult;
import com.example.cvrp.model.RouteLegEntity;
import com.example.cvrp.repository.AlgorithmResultRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AlgorithmResultService {
    private final AlgorithmResultRepository algorithmResultRepository;

    public AlgorithmResultService(AlgorithmResultRepository algorithmResultRepository) {
        this.algorithmResultRepository = algorithmResultRepository;
    }

    public AlgorithmResult saveResult(AlgorithmResult result) {
        return algorithmResultRepository.save(result);
    }

    public AlgorithmResult saveResult(AlgorithmResult result, List<RouteLegEntity> routeLegs) {
        result.setRouteLegs(routeLegs);
        return algorithmResultRepository.save(result);
    }
}
