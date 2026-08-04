package com.neueda.secureflow.monitoring;

import com.neueda.secureflow.config.MonitoringProperties;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class AmountRule {

    private final MonitoringProperties properties;

    public AmountRule(MonitoringProperties properties) {
        this.properties = properties;
    }

    public boolean isTriggered(BigDecimal amount) {
        return properties.amount().enabled()
                && amount.compareTo(properties.amount().threshold()) > 0;
    }
}
