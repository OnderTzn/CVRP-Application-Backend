package com.example.cvrp.controllerTest;

import com.example.cvrp.serviceTest.RoutingTestServiceImp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tests")
public class TestController {

    private final RoutingTestServiceImp routeTestService;

    @Autowired
    public TestController(RoutingTestServiceImp routeTestService) {
        this.routeTestService = routeTestService;
    }

    @GetMapping("/run")
    public ResponseEntity<String> runTests() {
        routeTestService.runTests();
        return ResponseEntity.ok("Tests started.");
    }
}
