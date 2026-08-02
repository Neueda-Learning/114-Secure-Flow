package com.neueda.secureflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class SecureFlowApplication {
    public static void main(String[] args) {
        SpringApplication.run(SecureFlowApplication.class, args);
    }
}
