package com.neueda.secureflow.transaction;

import com.neueda.secureflow.alert.AlertMapper;
import com.neueda.secureflow.common.BadRequestException;
import com.neueda.secureflow.common.PageResponse;
import com.neueda.secureflow.config.MonitoringProperties;
import com.neueda.secureflow.monitoring.MonitoringService;
import com.neueda.secureflow.transaction.dto.CreateTransactionRequest;
import com.neueda.secureflow.transaction.dto.TransactionCreatedResponse;
import com.neueda.secureflow.transaction.dto.TransactionResponse;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.Locale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class TransactionService {
    private final TransactionRepository repository;
    private final MonitoringService monitoringService;
    private final MonitoringProperties properties;
    private final Clock clock;

    @Autowired
    public TransactionService(TransactionRepository repository, MonitoringService monitoringService,
                              MonitoringProperties properties) {
        this(repository, monitoringService, properties, Clock.systemUTC());
    }

    TransactionService(TransactionRepository repository, MonitoringService monitoringService,
                       MonitoringProperties properties, Clock clock) {
        this.repository = repository;
        this.monitoringService = monitoringService;
        this.properties = properties;
        this.clock = clock;
    }

    @Transactional
    public TransactionCreatedResponse create(CreateTransactionRequest request) {
        if (!properties.amount().currency().equalsIgnoreCase(request.currency())) {
            throw new BadRequestException("SecureFlow currently supports "
                    + properties.amount().currency() + " transactions only");
        }
        Instant transactionTime = request.transactionTime() == null ? clock.instant() : request.transactionTime();
        boolean newPayee = !repository.existsByAccountIdAndPayeeId(
                request.accountId().trim(), request.payeeId().trim());

        TransactionEntity transaction = repository.save(new TransactionEntity(
                request.accountId().trim(), request.payeeId().trim(), request.amount(),
                request.currency().toUpperCase(Locale.ROOT), transactionTime,
                normalizeDescription(request.description()), clock.instant()));

        var alerts = monitoringService.evaluate(transaction, newPayee);
        return new TransactionCreatedResponse(TransactionMapper.toResponse(transaction),
                alerts.stream().map(AlertMapper::toSummary).toList());
    }

    @Transactional(readOnly = true)
    public PageResponse<TransactionResponse> search(String search, BigDecimal minAmount, BigDecimal maxAmount,
                                                     Instant from, Instant to, int page, int size) {
        if (minAmount != null && maxAmount != null && minAmount.compareTo(maxAmount) > 0) {
            throw new BadRequestException("minAmount cannot be greater than maxAmount");
        }
        if (from != null && to != null && from.isAfter(to)) {
            throw new BadRequestException("from cannot be after to");
        }
        PageRequest pageable = PageRequest.of(page, Math.min(size, 100),
                Sort.by(Sort.Direction.DESC, "transactionTime"));
        Page<TransactionEntity> result = repository.findAll(
                TransactionSpecifications.withFilters(search, minAmount, maxAmount, from, to), pageable);
        return PageResponse.from(result, TransactionMapper::toResponse);
    }

    private String normalizeDescription(String description) {
        return description == null || description.isBlank() ? null : description.trim();
    }
}
