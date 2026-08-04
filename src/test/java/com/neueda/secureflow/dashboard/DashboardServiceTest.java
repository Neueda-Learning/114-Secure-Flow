package com.neueda.secureflow.dashboard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.neueda.secureflow.alert.AlertRepository;
import com.neueda.secureflow.transaction.TransactionRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {
    @Mock TransactionRepository transactionRepository;
    @Mock AlertRepository alertRepository;

    @Test
    void summarizesCurrentIndiaDayUsingUtcStorageBoundaries() {
        when(alertRepository.countByStatusIn(any())).thenReturn(3L);
        when(transactionRepository.countByTransactionTimeBetween(any(), any())).thenReturn(12L);
        when(alertRepository.countByCreatedAtBetween(any(), any())).thenReturn(4L);
        when(transactionRepository.sumAmountBetween(any(), any())).thenReturn(new BigDecimal("2500.00"));
        var service = new DashboardService(transactionRepository, alertRepository,
                Clock.fixed(Instant.parse("2026-08-02T20:00:00Z"), ZoneOffset.UTC));

        var summary = service.summary();

        assertThat(summary.activeAlertCount()).isEqualTo(3);
        assertThat(summary.transactionCountToday()).isEqualTo(12);
        assertThat(summary.alertsToday()).isEqualTo(4);
        assertThat(summary.transactionVolumeToday()).isEqualByComparingTo("2500.00");
        var expectedFrom = Instant.parse("2026-08-02T18:30:00Z");
        var expectedTo = Instant.parse("2026-08-03T18:30:00Z");
        verify(transactionRepository).countByTransactionTimeBetween(expectedFrom, expectedTo);
        verify(alertRepository).countByCreatedAtBetween(expectedFrom, expectedTo);
        verify(transactionRepository).sumAmountBetween(expectedFrom, expectedTo);
    }
}
