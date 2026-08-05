package com.neueda.secureflow.demo;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/demo")
public class DemoDataController {
    private final DemoDataService service;

    public DemoDataController(DemoDataService service) {
        this.service = service;
    }

    @PostMapping("/seed")
    public DemoDataResponse seed() {
        return service.seed();
    }
}
