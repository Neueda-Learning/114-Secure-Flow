package com.neueda.secureflow.monitoring;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.neueda.secureflow.alert.AlertEntity;
import com.neueda.secureflow.alert.AlertService;
import com.neueda.secureflow.alert.AlertSeverity;
import com.neueda.secureflow.config.MonitoringProperties;
import com.neueda.secureflow.transaction.TransactionEntity;
import com.neueda.secureflow.transaction.TransactionRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MonitoringServiceTest {
    @Mock TransactionRepository transactionRepository;
    @Mock AlertService alertService;
    @Mock MonitoringRule rule;
    @Mock AlertEntity alert;

    @Test
    void buildsContextAndPersistsEveryRuleMatch() {
        var time = Instant.parse("2026-08-02T10:00:00Z");
        var transaction = new TransactionEntity("ACC", "PAYEE", BigDecimal.TEN, "INR", time, null, time);
        var properties = new MonitoringProperties(
                new MonitoringProperties.Amount(true, BigDecimal.TEN, "INR"),
                new MonitoringProperties.Velocity(true, 5, 10),
                new MonitoringProperties.NewPayee(true));
        var match = new RuleMatch(RuleType.NEW_PAYEE, "New payee", AlertSeverity.MEDIUM,
                "First use", "ACC", List.of(transaction));
        when(transactionRepository.findByAccountIdAndTransactionTimeBetweenOrderByTransactionTimeAsc(
                "ACC", time.minusSeconds(600), time)).thenReturn(List.of(transaction));
        when(rule.evaluate(any(), any())).thenReturn(Optional.of(match));
        when(alertService.create(match)).thenReturn(alert);

        var service = new MonitoringService(List.of(rule), transactionRepository, alertService, properties);
        var result = service.evaluate(transaction, true);

        assertThat(result).containsExactly(alert);
        verify(alertService).create(match);
    }
}
