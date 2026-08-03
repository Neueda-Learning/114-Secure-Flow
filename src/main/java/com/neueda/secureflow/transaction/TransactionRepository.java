package com.neueda.secureflow.transaction;

import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<TransactionEntity, Long> {
    boolean existsByAccountIdAndPayeeId(String accountId, String payeeId);

    List<TransactionEntity> findByAccountIdAndTransactionTimeBetweenOrderByTransactionTimeAsc(
            String accountId, Instant from, Instant to);
}
