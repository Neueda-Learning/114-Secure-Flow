package com.neueda.secureflow.alert;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.neueda.secureflow.common.BadRequestException;
import com.neueda.secureflow.common.ResourceNotFoundException;
import com.neueda.secureflow.monitoring.RuleMatch;
import com.neueda.secureflow.monitoring.RuleType;
import com.neueda.secureflow.transaction.TransactionEntity;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AlertServiceTest {
    private static final Instant NOW = Instant.parse("2026-08-02T10:00:00Z");

    @Mock AlertRepository repository;
    AlertService service;
    AlertEntity alert;

    @BeforeEach
    void setUp() {
        service = new AlertService(repository, Clock.fixed(NOW, ZoneOffset.UTC));
        alert = new AlertEntity(RuleType.AMOUNT_THRESHOLD, "High amount", AlertSeverity.HIGH,
                "Threshold exceeded", "ACC-1", NOW.minusSeconds(30), List.of(transaction()));
    }

    @Test
    void createsOpenAlertWithInitialHistory() {
        when(repository.save(any(AlertEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        AlertEntity created = service.create(match());

        assertThat(created.getStatus()).isEqualTo(AlertStatus.OPEN);
        assertThat(created.getHistory()).singleElement().satisfies(item -> {
            assertThat(item.getPreviousStatus()).isNull();
            assertThat(item.getNewStatus()).isEqualTo(AlertStatus.OPEN);
        });
    }

    @Test
    void followsCompleteLifecycleAndRecordsHistory() {
        when(repository.findById(7L)).thenReturn(Optional.of(alert));
        when(repository.save(any(AlertEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertThat(service.transition(7, AlertStatus.ACKNOWLEDGED, null).status())
                .isEqualTo(AlertStatus.ACKNOWLEDGED);
        assertThat(service.transition(7, AlertStatus.INVESTIGATING, null).status())
                .isEqualTo(AlertStatus.INVESTIGATING);
        var closed = service.transition(7, AlertStatus.CLOSED, "Confirmed legitimate");

        assertThat(closed.status()).isEqualTo(AlertStatus.CLOSED);
        assertThat(closed.resolutionNotes()).isEqualTo("Confirmed legitimate");
        assertThat(closed.history()).hasSize(4);
    }

    @Test
    void rejectsIllegalTransition() {
        when(repository.findById(7L)).thenReturn(Optional.of(alert));

        assertThatThrownBy(() -> service.transition(7, AlertStatus.CLOSED, "Not allowed"))
                .isInstanceOf(InvalidAlertTransitionException.class)
                .hasMessageContaining("OPEN");
    }

    @Test
    void terminalStatusRequiresMeaningfulNotes() {
        when(repository.findById(7L)).thenReturn(Optional.of(alert));
        when(repository.save(any(AlertEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        service.transition(7, AlertStatus.ACKNOWLEDGED, null);

        assertThatThrownBy(() -> service.transition(7, AlertStatus.DISMISSED, "x"))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void missingAlertReturnsNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.get(99L)).isInstanceOf(ResourceNotFoundException.class);
    }

    private RuleMatch match() {
        return new RuleMatch(RuleType.AMOUNT_THRESHOLD, "High amount", AlertSeverity.HIGH,
                "Threshold exceeded", "ACC-1", List.of(transaction()));
    }

    private TransactionEntity transaction() {
        return new TransactionEntity("ACC-1", "PAYEE-1", new BigDecimal("15000"), "INR",
                NOW, "Test", NOW);
    }
}
