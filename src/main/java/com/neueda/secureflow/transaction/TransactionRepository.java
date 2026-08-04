package com.neueda.secureflow.transaction;

import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TransactionRepository extends JpaRepository<TransactionEntity, Long>,
        JpaSpecificationExecutor<TransactionEntity> {
    boolean existsByAccountIdAndPayeeId(String accountId, String payeeId);

    List<TransactionEntity> findByAccountIdAndTransactionTimeBetweenOrderByTransactionTimeAsc(
            String accountId, Instant from, Instant to);
}
