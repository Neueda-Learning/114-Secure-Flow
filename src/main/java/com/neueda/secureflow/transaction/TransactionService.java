package com.neueda.secureflow.transaction;

import com.neueda.secureflow.alert.dto.AlertSummaryResponse;
import com.neueda.secureflow.common.ApiException;
import com.neueda.secureflow.common.PageResponse;
import com.neueda.secureflow.config.RulesConfig;
import com.neueda.secureflow.monitoring.MonitoringService;
import com.neueda.secureflow.transaction.dto.CreateTransactionRequest;
import com.neueda.secureflow.transaction.dto.CreatedTransactionResponse;
import com.neueda.secureflow.transaction.dto.TransactionResponse;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Locale;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionService {
    private final TransactionRepository repository;
    private final MonitoringService monitoringService;
    private final RulesConfig rules;

    public TransactionService(
            TransactionRepository repository,
            MonitoringService monitoringService,
            RulesConfig rules) {
        this.repository = repository;
        this.monitoringService = monitoringService;
        this.rules = rules;
    }

    @Transactional
    public CreatedTransactionResponse create(CreateTransactionRequest request) {
        String accountId = request.accountId().trim();
        String payeeId = request.payeeId().trim();
        String currency = request.currency().toUpperCase(Locale.ROOT);

        if (!currency.equals(rules.currency())) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid request",
                    "SecureFlow currently supports " + rules.currency() + " only");
        }

        boolean isNewPayee =
                !repository.existsByAccountIdAndPayeeId(accountId, payeeId);
        Instant now = Instant.now();

        TransactionEntity transaction = new TransactionEntity(
                accountId,
                payeeId,
                request.amount(),
                currency,
                now,
                clean(request.description()),
                now);

        transaction = repository.save(transaction);
        var alerts = monitoringService.check(transaction, isNewPayee)
                .stream()
                .map(AlertSummaryResponse::from)
                .toList();

        return new CreatedTransactionResponse(
                TransactionResponse.from(transaction), alerts);
    }

    @Transactional(readOnly = true)
    public PageResponse<TransactionResponse> search(
            String search,
            Long transactionId,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            Instant from,
            Instant to,
            int page,
            int size) {

        if (minAmount != null && maxAmount != null
                && minAmount.compareTo(maxAmount) > 0) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid request",
                    "minAmount cannot be greater than maxAmount");
        }

        if (from != null && to != null && from.isAfter(to)) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid request",
                    "from cannot be after to");
        }

        PageRequest request = PageRequest.of(
                page,
                Math.min(size, 100),
                Sort.by("transactionTime").descending());

        return PageResponse.from(
                repository.search(
                                clean(search), transactionId, minAmount, maxAmount,
                                from, to, request)
                        .map(TransactionResponse::from));
    }

    private String clean(String text) {
        return text == null || text.isBlank() ? null : text.trim();
    }
}
