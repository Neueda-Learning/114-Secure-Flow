package com.neueda.secureflow.alert.dto;

import com.neueda.secureflow.alert.AlertEntity;
import com.neueda.secureflow.alert.AlertSeverity;
import com.neueda.secureflow.alert.AlertStatus;
import com.neueda.secureflow.monitoring.RuleType;
import java.time.Instant;
import java.util.List;

public record AlertSummaryResponse(
        Long id,
        RuleType ruleType,
        String ruleName,
        AlertSeverity severity,
        AlertStatus status,
        String message,
        String accountId,
        List<Long> transactionIds,
        Instant createdAt
) {
    public static AlertSummaryResponse from(AlertEntity alert) {
        List<Long> transactionIds = alert.getTransactions().stream()
                .map(transaction -> transaction.getId())
                .sorted()
                .toList();

        return new AlertSummaryResponse(
                alert.getId(), alert.getRuleType(), alert.getRuleName(),
                alert.getSeverity(), alert.getStatus(), alert.getMessage(),
                alert.getAccountId(), transactionIds, alert.getCreatedAt());
    }
}
