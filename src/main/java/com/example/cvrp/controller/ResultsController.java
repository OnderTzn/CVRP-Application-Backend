package com.example.cvrp.controller;

import com.example.cvrp.model.AlgorithmResult;
import com.example.cvrp.service.AlgorithmResultServiceImp;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@CrossOrigin("http://localhost:3000")
@RequestMapping("/results")
public class ResultsController {

    private final AlgorithmResultServiceImp algorithmResultServiceImp;

    public ResultsController(AlgorithmResultServiceImp algorithmResultServiceImp) {
        this.algorithmResultServiceImp = algorithmResultServiceImp;
    }

    @GetMapping("/all")
    public ResponseEntity<List<AlgorithmResult>> getAllResults() {
        List<AlgorithmResult> resultsList = algorithmResultServiceImp.findAllResults();
        return new ResponseEntity<>(resultsList, HttpStatus.OK);
    }
}
