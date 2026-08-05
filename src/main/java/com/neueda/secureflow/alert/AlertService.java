package com.neueda.secureflow.alert;

import com.neueda.secureflow.alert.dto.AlertDetailResponse;
import com.neueda.secureflow.alert.dto.AlertSummaryResponse;
import com.neueda.secureflow.common.ApiException;
import com.neueda.secureflow.common.PageResponse;
import com.neueda.secureflow.monitoring.RuleType;
import com.neueda.secureflow.transaction.TransactionEntity;
import java.time.Instant;
import java.util.Collection;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AlertService {
    private final AlertRepository repository;

    public AlertService(AlertRepository repository) {
        this.repository = repository;
    }

    public AlertEntity create(
            RuleType ruleType,
            String ruleName,
            AlertSeverity severity,
            String message,
            String accountId,
            Collection<TransactionEntity> transactions) {

        AlertEntity alert = new AlertEntity(
                ruleType, ruleName, severity, message,
                accountId, Instant.now(), transactions);
        return repository.save(alert);
    }

    @Transactional(readOnly = true)
    public PageResponse<AlertSummaryResponse> search(
            AlertStatus status, AlertSeverity severity, int page, int size) {

        PageRequest request = PageRequest.of(
                page, Math.min(size, 100), Sort.by("createdAt").descending());

        return PageResponse.from(
                repository.search(status, severity, request).map(AlertSummaryResponse::from));
    }

    @Transactional(readOnly = true)
    public AlertDetailResponse get(long id) {
        return AlertDetailResponse.from(find(id));
    }

    @Transactional
    public AlertDetailResponse updateStatus(
            long id, AlertStatus nextStatus, String resolutionNotes) {

        AlertEntity alert = find(id);

        if (!isAllowed(alert.getStatus(), nextStatus)) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "Invalid alert transition",
                    "Cannot move alert from " + alert.getStatus() + " to " + nextStatus);
        }

        String notes = clean(resolutionNotes);
        boolean isFinished = nextStatus == AlertStatus.CLOSED
                || nextStatus == AlertStatus.DISMISSED;

        if (isFinished && (notes == null || notes.length() < 3)) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid request",
                    "Resolution notes of at least 3 characters are required");
        }

        alert.changeStatus(nextStatus, notes, Instant.now());
        return AlertDetailResponse.from(repository.save(alert));
    }

    private AlertEntity find(long id) {
        return repository.findById(id).orElseThrow(() ->
                new ApiException(
                        HttpStatus.NOT_FOUND,
                        "Resource not found",
                        "Alert " + id + " was not found"));
    }

    private boolean isAllowed(AlertStatus current, AlertStatus next) {
        return switch (current) {
            case OPEN -> next == AlertStatus.ACKNOWLEDGED;
            case ACKNOWLEDGED ->
                    next == AlertStatus.INVESTIGATING || next == AlertStatus.DISMISSED;
            case INVESTIGATING ->
                    next == AlertStatus.CLOSED || next == AlertStatus.DISMISSED;
            case CLOSED, DISMISSED -> false;
        };
    }

    private String clean(String text) {
        return text == null || text.isBlank() ? null : text.trim();
    }
}
