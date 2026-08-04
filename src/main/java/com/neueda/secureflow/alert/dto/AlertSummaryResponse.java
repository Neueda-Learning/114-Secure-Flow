package com.neueda.secureflow.alert.dto;

import com.neueda.secureflow.alert.AlertSeverity;
import com.neueda.secureflow.alert.AlertStatus;
import com.neueda.secureflow.monitoring.RuleType;
import java.time.Instant;

public record AlertSummaryResponse(
        Long id,
        RuleType ruleType,
        String ruleName,
        AlertSeverity severity,
        AlertStatus status,
        String message,
        String accountId,
        Instant createdAt
) {}
