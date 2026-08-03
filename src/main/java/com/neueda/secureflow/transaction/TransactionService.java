package com.neueda.secureflow.transaction;

import java.time.Instant;
import java.util.Locale;
import org.springframework.stereotype.Service;

@Service
public class TransactionService {
    private final TransactionRepository repository;

    public TransactionService(TransactionRepository repository) {
        this.repository = repository;
    }

    public TransactionResponse create(CreateTransactionRequest request) {
        TransactionEntity transaction = new TransactionEntity(
                request.accountId().trim(),
                request.payeeId().trim(),
                request.amount(),
                request.currency().toUpperCase(Locale.ROOT),
                request.transactionTime(),
                request.description(),
                Instant.now());

        return TransactionResponse.from(repository.save(transaction));
    }
}
