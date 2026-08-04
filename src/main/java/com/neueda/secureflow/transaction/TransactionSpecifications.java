package com.neueda.secureflow.transaction;

import java.math.BigDecimal;
import java.time.Instant;
import org.springframework.data.jpa.domain.Specification;

final class TransactionSpecifications {
    private TransactionSpecifications() {}

    static Specification<TransactionEntity> withFilters(
            String search, BigDecimal minAmount, BigDecimal maxAmount, Instant from, Instant to) {
        Specification<TransactionEntity> result = Specification.unrestricted();

        if (search != null && !search.isBlank()) {
            String pattern = "%" + search.trim().toLowerCase() + "%";
            result = result.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("accountId")), pattern),
                    cb.like(cb.lower(root.get("payeeId")), pattern),
                    cb.like(cb.lower(cb.coalesce(root.get("description"), "")), pattern)
            ));
        }
        if (minAmount != null) {
            result = result.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("amount"), minAmount));
        }
        if (maxAmount != null) {
            result = result.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("amount"), maxAmount));
        }
        if (from != null) {
            result = result.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("transactionTime"), from));
        }
        if (to != null) {
            result = result.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("transactionTime"), to));
        }
        return result;
    }
}
