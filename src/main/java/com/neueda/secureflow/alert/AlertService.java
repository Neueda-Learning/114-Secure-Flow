package com.neueda.secureflow.alert;

import com.neueda.secureflow.alert.dto.AlertDetailResponse;
import com.neueda.secureflow.alert.dto.AlertSummaryResponse;
import com.neueda.secureflow.common.BadRequestException;
import com.neueda.secureflow.common.PageResponse;
import com.neueda.secureflow.common.ResourceNotFoundException;
import com.neueda.secureflow.monitoring.RuleMatch;
import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class AlertService {
    private static final Map<AlertStatus, Set<AlertStatus>> ALLOWED_TRANSITIONS = Map.of(
            AlertStatus.OPEN, Set.of(AlertStatus.ACKNOWLEDGED),
            AlertStatus.ACKNOWLEDGED, Set.of(AlertStatus.INVESTIGATING, AlertStatus.DISMISSED),
            AlertStatus.INVESTIGATING, Set.of(AlertStatus.CLOSED, AlertStatus.DISMISSED),
            AlertStatus.CLOSED, Set.of(),
            AlertStatus.DISMISSED, Set.of()
    );

    private final AlertRepository repository;
    private final Clock clock;

    @Autowired
    public AlertService(AlertRepository repository) {
        this(repository, Clock.systemUTC());
    }

    AlertService(AlertRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Transactional
    public AlertEntity create(RuleMatch match) {
        return repository.save(new AlertEntity(match.ruleType(), match.ruleName(), match.severity(),
                match.message(), match.accountId(), clock.instant(), match.transactions()));
    }

    @Transactional(readOnly = true)
    public PageResponse<AlertSummaryResponse> search(AlertStatus status, AlertSeverity severity, int page, int size) {
        var pageable = PageRequest.of(page, Math.min(size, 100), Sort.by(Sort.Direction.DESC, "createdAt"));
        var result = repository.findAll(AlertSpecifications.withFilters(status, severity), pageable);
        return PageResponse.from(result, AlertMapper::toSummary);
    }

    @Transactional(readOnly = true)
    public AlertDetailResponse get(long id) {
        return AlertMapper.toDetail(find(id));
    }

    @Transactional
    public AlertDetailResponse transition(long id, AlertStatus target, String resolutionNotes) {
        AlertEntity alert = find(id);
        if (!ALLOWED_TRANSITIONS.get(alert.getStatus()).contains(target)) {
            throw new InvalidAlertTransitionException(
                    "Cannot move alert from " + alert.getStatus() + " to " + target);
        }

        String notes = resolutionNotes == null ? null : resolutionNotes.trim();
        if ((target == AlertStatus.CLOSED || target == AlertStatus.DISMISSED)
                && (notes == null || notes.length() < 3)) {
            throw new BadRequestException("Resolution notes of at least 3 characters are required");
        }

        alert.transitionTo(target, clock.instant(), notes);
        return AlertMapper.toDetail(repository.save(alert));
    }

    private AlertEntity find(long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alert " + id + " was not found"));
    }
}
