package com.neueda.secureflow.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class MonitoringPropertiesTest {
    @Autowired
    private MonitoringProperties properties;

    @Test
    void loadsTheThreeRuleConfigurations() {
        assertTrue(properties.amount().enabled());
        assertEquals(0, new BigDecimal("10000.00").compareTo(properties.amount().threshold()));
        assertEquals("USD", properties.amount().currency());
        assertTrue(properties.velocity().enabled());
        assertEquals(5, properties.velocity().maximumTransactions());
        assertEquals(10, properties.velocity().windowMinutes());
        assertTrue(properties.newPayee().enabled());
    }
}
