package com.neueda.secureflow.transaction.dto;

import com.neueda.secureflow.transaction.TransactionEntity;
import java.math.BigDecimal;
import java.time.Instant;

public record TransactionResponse(
        Long id,
        String accountId,
        String payeeId,
        BigDecimal amount,
        String currency,
        Instant transactionTime,
        String description,
        Instant createdAt
) {
    public static TransactionResponse from(TransactionEntity transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getAccountId(),
                transaction.getPayeeId(),
                transaction.getAmount(),
                transaction.getCurrency(),
                transaction.getTransactionTime(),
                transaction.getDescription(),
                transaction.getCreatedAt());
    }
}
