package com.neueda.secureflow.demo;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "demo.seed-on-startup", havingValue = "true")
public class DemoDataStartup implements ApplicationRunner {
    private final DemoDataService service;

    public DemoDataStartup(DemoDataService service) {
        this.service = service;
    }

    @Override
    public void run(ApplicationArguments arguments) {
        service.seedIfEmpty();
    }
}
