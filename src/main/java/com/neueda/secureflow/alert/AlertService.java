package com.neueda.secureflow.alert;

import com.neueda.secureflow.common.BadRequestException;
import com.neueda.secureflow.common.PageResponse;
import com.neueda.secureflow.common.ResourceNotFoundException;
import com.neueda.secureflow.monitoring.RuleMatch;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AlertService {
    private static final Map<AlertStatus, Set<AlertStatus>> ALLOWED = Map.of(
            AlertStatus.OPEN, Set.of(AlertStatus.ACKNOWLEDGED),
            AlertStatus.ACKNOWLEDGED, Set.of(AlertStatus.INVESTIGATING, AlertStatus.DISMISSED),
            AlertStatus.INVESTIGATING, Set.of(AlertStatus.CLOSED, AlertStatus.DISMISSED),
            AlertStatus.CLOSED, Set.of(), AlertStatus.DISMISSED, Set.of());

    private final AlertRepository repository;

    public AlertService(AlertRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public AlertEntity create(RuleMatch match) {
        return repository.save(new AlertEntity(match, Instant.now()));
    }

    @Transactional(readOnly = true)
    public PageResponse<AlertResponse> list(AlertStatus status, AlertSeverity severity, int page, int size) {
        var pageable = PageRequest.of(page, Math.min(size, 100),
                Sort.by(Sort.Direction.DESC, "createdAt"));
        var result = repository.findAll(AlertSpecifications.withFilters(status, severity), pageable);
        return PageResponse.from(result, AlertResponse::from);
    }

    @Transactional(readOnly = true)
    public AlertResponse get(long id) {
        return AlertResponse.from(find(id));
    }

    @Transactional
    public AlertResponse transition(long id, AlertStatus next, String resolutionNotes) {
        AlertEntity alert = find(id);
        if (!ALLOWED.get(alert.getStatus()).contains(next)) {
            throw new InvalidAlertTransitionException(
                    "Cannot move alert from " + alert.getStatus() + " to " + next);
        }
        String notes = resolutionNotes == null ? null : resolutionNotes.trim();
        if ((next == AlertStatus.CLOSED || next == AlertStatus.DISMISSED)
                && (notes == null || notes.length() < 3)) {
            throw new BadRequestException("Resolution notes are required");
        }
        alert.transition(next, Instant.now(), notes);
        return AlertResponse.from(repository.save(alert));
    }

    private AlertEntity find(long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alert " + id + " was not found"));
    }
}
