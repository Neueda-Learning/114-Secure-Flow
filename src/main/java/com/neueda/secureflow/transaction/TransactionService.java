package com.neueda.secureflow.transaction;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import com.neueda.secureflow.alert.AlertResponse;
import com.neueda.secureflow.monitoring.MonitoringService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Sort;

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
    public List<TransactionResponse> list(String search) {
        String term = search == null ? "" : search.trim().toLowerCase(Locale.ROOT);
        return repository.findAll(Sort.by(Sort.Direction.DESC, "transactionTime")).stream()
                .filter(transaction -> term.isEmpty()
                        || transaction.getAccountId().toLowerCase(Locale.ROOT).contains(term)
                        || transaction.getPayeeId().toLowerCase(Locale.ROOT).contains(term)
                        || (transaction.getDescription() != null
                            && transaction.getDescription().toLowerCase(Locale.ROOT).contains(term)))
                .map(TransactionResponse::from).toList();
    }
}
