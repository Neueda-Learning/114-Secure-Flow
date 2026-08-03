package com.neueda.secureflow.config;

import java.math.BigDecimal;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "monitoring.rules")
public record MonitoringProperties(
        Amount amount,
        Velocity velocity,
        NewPayee newPayee
) {
    public record Amount(boolean enabled, BigDecimal threshold, String currency) {
    }

    public record Velocity(boolean enabled, int maximumTransactions, int windowMinutes) {
    }

    public record NewPayee(boolean enabled) {
    }
}
