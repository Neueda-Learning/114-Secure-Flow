package com.neueda.secureflow.transaction;

import com.neueda.secureflow.transaction.dto.TransactionResponse;

public final class TransactionMapper {
    private TransactionMapper() {}

    public static TransactionResponse toResponse(TransactionEntity entity) {
        return new TransactionResponse(
                entity.getId(), entity.getAccountId(), entity.getPayeeId(), entity.getAmount(),
                entity.getCurrency(), entity.getTransactionTime(), entity.getDescription(), entity.getCreatedAt());
    }
}
