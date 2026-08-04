package com.neueda.secureflow.alert;

import com.neueda.secureflow.monitoring.RuleType;
import com.neueda.secureflow.transaction.TransactionResponse;
import java.time.Instant;
import java.util.List;

public record AlertResponse(Long id, RuleType ruleType, String ruleName, AlertSeverity severity,
                            AlertStatus status, String message, String accountId, Instant createdAt,
                            Instant acknowledgedAt, Instant investigatingAt, Instant closedAt,
                            String resolutionNotes, List<TransactionResponse> triggeringTransactions,
                            List<AlertHistoryResponse> history) {

    public static AlertResponse from(AlertEntity alert) {
        return new AlertResponse(alert.getId(), alert.getRuleType(), alert.getRuleName(), alert.getSeverity(),
                alert.getStatus(), alert.getMessage(), alert.getAccountId(), alert.getCreatedAt(),
                alert.getAcknowledgedAt(), alert.getInvestigatingAt(), alert.getClosedAt(),
                alert.getResolutionNotes(), alert.getTriggeringTransactions().stream()
                        .map(TransactionResponse::from).toList(), alert.getHistory().stream()
                        .map(item -> new AlertHistoryResponse(item.getPreviousStatus(), item.getNewStatus(),
                                item.getChangedAt(), item.getNote())).toList());
    }
}
