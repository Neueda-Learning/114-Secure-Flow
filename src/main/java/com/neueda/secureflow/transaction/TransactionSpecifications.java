package com.neueda.secureflow.transaction;

import java.math.BigDecimal;
import java.time.Instant;
import org.springframework.data.jpa.domain.Specification;

final class TransactionSpecifications {
    private TransactionSpecifications() { }

    static Specification<TransactionEntity> withFilters(String search, BigDecimal minAmount,
                                                         BigDecimal maxAmount, Instant from, Instant to) {
        Specification<TransactionEntity> result = Specification.unrestricted();
        if (search != null && !search.isBlank()) {
            String term = "%" + search.trim().toLowerCase() + "%";
            result = result.and((root, query, builder) -> builder.or(
                    builder.like(builder.lower(root.get("accountId")), term),
                    builder.like(builder.lower(root.get("payeeId")), term),
                    builder.like(builder.lower(root.get("description")), term)));
        }
        if (minAmount != null) {
            result = result.and((root, query, builder) ->
                    builder.greaterThanOrEqualTo(root.get("amount"), minAmount));
        }
        if (maxAmount != null) {
            result = result.and((root, query, builder) ->
                    builder.lessThanOrEqualTo(root.get("amount"), maxAmount));
        }
        if (from != null) {
            result = result.and((root, query, builder) ->
                    builder.greaterThanOrEqualTo(root.get("transactionTime"), from));
        }
        if (to != null) {
            result = result.and((root, query, builder) ->
                    builder.lessThanOrEqualTo(root.get("transactionTime"), to));
        }
        return result;
    }
}
