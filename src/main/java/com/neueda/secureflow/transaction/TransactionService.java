package com.neueda.secureflow.transaction;

import com.neueda.secureflow.alert.AlertResponse;
import com.neueda.secureflow.common.BadRequestException;
import com.neueda.secureflow.common.PageResponse;
import com.neueda.secureflow.monitoring.MonitoringService;
import java.math.BigDecimal;
import java.time.Instant;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionService {
    private final TransactionRepository repository;
    private final MonitoringService monitoring;

    public TransactionService(TransactionRepository repository, MonitoringService monitoring) {
        this.repository = repository;
        this.monitoring = monitoring;
    }

    @Transactional
    public TransactionCreatedResponse create(CreateTransactionRequest request) {
        boolean newPayee = !repository.existsByAccountIdAndPayeeId(
                request.accountId().trim(), request.payeeId().trim());
        TransactionEntity transaction = new TransactionEntity(
                request.accountId().trim(),
                request.payeeId().trim(),
                request.amount(),
                request.currency().toUpperCase(Locale.ROOT),
                request.transactionTime(),
                request.description(),
                Instant.now());

        transaction = repository.save(transaction);
        List<AlertResponse> generatedAlerts = monitoring.evaluate(transaction, newPayee).stream()
                .map(AlertResponse::from).toList();
        return new TransactionCreatedResponse(TransactionResponse.from(transaction), generatedAlerts);
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
        var pageable = PageRequest.of(page, Math.min(size, 100),
                Sort.by(Sort.Direction.DESC, "transactionTime"));
        var result = repository.findAll(
                TransactionSpecifications.withFilters(search, minAmount, maxAmount, from, to), pageable);
        return PageResponse.from(result, TransactionResponse::from);
    }
}
