package com.neueda.secureflow.alert.dto;

import com.neueda.secureflow.alert.AlertSeverity;
import com.neueda.secureflow.alert.AlertStatus;
import com.neueda.secureflow.monitoring.RuleType;
import com.neueda.secureflow.transaction.dto.TransactionResponse;
import java.time.Instant;
import java.util.List;

public record AlertDetailResponse(
        Long id,
        RuleType ruleType,
        String ruleName,
        AlertSeverity severity,
        AlertStatus status,
        String message,
        String accountId,
        Instant createdAt,
        Instant acknowledgedAt,
        Instant investigatingAt,
        Instant closedAt,
        String resolutionNotes,
        List<TransactionResponse> triggeringTransactions,
        List<AlertHistoryResponse> history
) {}
