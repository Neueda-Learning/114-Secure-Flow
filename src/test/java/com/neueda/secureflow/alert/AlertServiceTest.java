package com.neueda.secureflow.alert;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.neueda.secureflow.common.BadRequestException;
import com.neueda.secureflow.common.ResourceNotFoundException;
import com.neueda.secureflow.monitoring.RuleMatch;
import com.neueda.secureflow.monitoring.RuleType;
import com.neueda.secureflow.transaction.TransactionEntity;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class AlertServiceTest {
    private final AlertRepository repository = mock(AlertRepository.class);
    private final AlertService service = new AlertService(repository);

    @Test
    void followsTheHappyLifecycleAndSavesHistory() {
        AlertEntity alert = alert();
        when(repository.findById(1L)).thenReturn(Optional.of(alert));
        when(repository.save(any(AlertEntity.class))).thenAnswer(call -> call.getArgument(0));

        assertEquals(AlertStatus.ACKNOWLEDGED, service.transition(1, AlertStatus.ACKNOWLEDGED, null).status());
        assertEquals(AlertStatus.INVESTIGATING, service.transition(1, AlertStatus.INVESTIGATING, null).status());
        AlertResponse closed = service.transition(1, AlertStatus.CLOSED, "Checked and safe");

        assertEquals(AlertStatus.CLOSED, closed.status());
        assertEquals(4, closed.history().size());
        assertEquals("Checked and safe", closed.resolutionNotes());
    }

    @Test
    void rejectsInvalidTransitionsAndMissingNotes() {
        AlertEntity open = alert();
        when(repository.findById(1L)).thenReturn(Optional.of(open));
        assertThrows(InvalidAlertTransitionException.class,
                () -> service.transition(1, AlertStatus.CLOSED, "Too early"));

        open.transition(AlertStatus.ACKNOWLEDGED, Instant.now(), null);
        assertThrows(BadRequestException.class,
                () -> service.transition(1, AlertStatus.DISMISSED, ""));
        assertThrows(ResourceNotFoundException.class, () -> service.get(99));
    }

    private AlertEntity alert() {
        Instant now = Instant.parse("2026-08-04T10:00:00Z");
        TransactionEntity transaction = new TransactionEntity("ACC-1", "PAYEE-1", BigDecimal.TEN,
                "INR", now, null, now);
        RuleMatch match = new RuleMatch(RuleType.NEW_PAYEE, "New payee", AlertSeverity.MEDIUM,
                "First payment", "ACC-1", List.of(transaction));
        return new AlertEntity(match, now);
    }
}
