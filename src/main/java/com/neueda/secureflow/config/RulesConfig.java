package com.neueda.secureflow.config;

import java.math.BigDecimal;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("monitoring")
public record RulesConfig(
        BigDecimal amountLimit,
        String currency,
        int maxTransactions,
        int windowMinutes
) {}
