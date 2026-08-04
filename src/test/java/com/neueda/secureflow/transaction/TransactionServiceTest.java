package com.neueda.secureflow.transaction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.neueda.secureflow.common.BadRequestException;
import com.neueda.secureflow.config.MonitoringProperties;
import com.neueda.secureflow.monitoring.MonitoringService;
import com.neueda.secureflow.transaction.dto.CreateTransactionRequest;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {
    private static final Instant NOW = Instant.parse("2026-08-02T10:00:00Z");

    @Mock TransactionRepository repository;
    @Mock MonitoringService monitoringService;
    TransactionService service;
    MonitoringProperties properties;

    @BeforeEach
    void setUp() {
        properties = new MonitoringProperties(
                new MonitoringProperties.Amount(true, new BigDecimal("10000"), "INR"),
                new MonitoringProperties.Velocity(true, 5, 10),
                new MonitoringProperties.NewPayee(true));
        service = new TransactionService(repository, monitoringService, properties,
                Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void savesNormalizedTransactionThenEvaluatesRules() {
        when(repository.existsByAccountIdAndPayeeId("ACC-1", "PAYEE-1")).thenReturn(false);
        when(repository.save(any(TransactionEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(monitoringService.evaluate(any(), org.mockito.ArgumentMatchers.eq(true))).thenReturn(List.of());

        var result = service.create(new CreateTransactionRequest(" ACC-1 ", " PAYEE-1 ",
                new BigDecimal("100.00"), "inr", null, " Test payment "));

        assertThat(result.transaction().accountId()).isEqualTo("ACC-1");
        assertThat(result.transaction().currency()).isEqualTo("INR");
        assertThat(result.transaction().transactionTime()).isEqualTo(NOW);
        verify(monitoringService).evaluate(any(), org.mockito.ArgumentMatchers.eq(true));
    }

    @Test
    void rejectsUnsupportedCurrency() {
        assertThatThrownBy(() -> service.create(new CreateTransactionRequest("ACC", "PAYEE",
                BigDecimal.ONE, "EUR", NOW, null))).isInstanceOf(BadRequestException.class);
    }

    @Test
    void rejectsInvertedAmountAndDateRanges() {
        assertThatThrownBy(() -> service.search(null, BigDecimal.TEN, BigDecimal.ONE,
                null, null, 0, 20)).isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> service.search(null, null, null,
                NOW, NOW.minusSeconds(1), 0, 20)).isInstanceOf(BadRequestException.class);
    }

    @Test
    void mapsPagedSearchResults() {
        var entity = new TransactionEntity("ACC", "PAYEE", BigDecimal.TEN, "INR", NOW, null, NOW);
        when(repository.findAll(any(org.springframework.data.jpa.domain.Specification.class),
                any(org.springframework.data.domain.Pageable.class))).thenReturn(new PageImpl<>(List.of(entity)));

        var result = service.search("acc", null, null, null, null, 0, 20);

        assertThat(result.items()).singleElement().extracting(item -> item.accountId()).isEqualTo("ACC");
    }
}
