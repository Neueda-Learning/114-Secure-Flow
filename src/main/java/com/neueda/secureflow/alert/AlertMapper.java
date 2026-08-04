package com.neueda.secureflow.alert;

import com.neueda.secureflow.alert.dto.AlertDetailResponse;
import com.neueda.secureflow.alert.dto.AlertHistoryResponse;
import com.neueda.secureflow.alert.dto.AlertSummaryResponse;
import com.neueda.secureflow.transaction.TransactionMapper;
import java.util.Comparator;

public final class AlertMapper {
    private AlertMapper() {}

    public static AlertSummaryResponse toSummary(AlertEntity alert) {
        return new AlertSummaryResponse(alert.getId(), alert.getRuleType(), alert.getRuleName(),
                alert.getSeverity(), alert.getStatus(), alert.getMessage(), alert.getAccountId(), alert.getCreatedAt());
    }

    public static AlertDetailResponse toDetail(AlertEntity alert) {
        var transactions = alert.getTriggeringTransactions().stream()
                .sorted(Comparator.comparing(com.neueda.secureflow.transaction.TransactionEntity::getTransactionTime))
                .map(TransactionMapper::toResponse).toList();
        var history = alert.getHistory().stream()
                .map(item -> new AlertHistoryResponse(item.getPreviousStatus(), item.getNewStatus(),
                        item.getChangedAt(), item.getNote()))
                .toList();
        return new AlertDetailResponse(alert.getId(), alert.getRuleType(), alert.getRuleName(),
                alert.getSeverity(), alert.getStatus(), alert.getMessage(), alert.getAccountId(),
                alert.getCreatedAt(), alert.getAcknowledgedAt(), alert.getInvestigatingAt(),
                alert.getClosedAt(), alert.getResolutionNotes(), transactions, history);
    }
}
