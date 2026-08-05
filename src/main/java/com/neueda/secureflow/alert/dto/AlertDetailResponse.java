package com.neueda.secureflow.alert.dto;

import com.neueda.secureflow.alert.AlertEntity;
import com.neueda.secureflow.alert.AlertSeverity;
import com.neueda.secureflow.alert.AlertStatus;
import com.neueda.secureflow.monitoring.RuleType;
import com.neueda.secureflow.transaction.TransactionEntity;
import com.neueda.secureflow.transaction.dto.TransactionResponse;
import java.time.Instant;
import java.util.Comparator;
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
) {
    public static AlertDetailResponse from(AlertEntity alert) {
        List<TransactionResponse> transactions = alert.getTransactions().stream()
                .sorted(Comparator.comparing(TransactionEntity::getTransactionTime))
                .map(TransactionResponse::from)
                .toList();

        List<AlertHistoryResponse> history = alert.getHistory().stream()
                .map(AlertHistoryResponse::from)
                .toList();

        return new AlertDetailResponse(
                alert.getId(), alert.getRuleType(), alert.getRuleName(),
                alert.getSeverity(), alert.getStatus(), alert.getMessage(),
                alert.getAccountId(), alert.getCreatedAt(), alert.getAcknowledgedAt(),
                alert.getInvestigatingAt(), alert.getClosedAt(), alert.getResolutionNotes(),
                transactions, history);
    }
}
