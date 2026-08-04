package com.neueda.secureflow.transaction;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TransactionRepository extends JpaRepository<TransactionEntity, Long>,
        JpaSpecificationExecutor<TransactionEntity> {

    boolean existsByAccountIdAndPayeeId(String accountId, String payeeId);

    List<TransactionEntity> findByAccountIdAndTransactionTimeBetweenOrderByTransactionTimeAsc(
            String accountId, Instant from, Instant to);

    long countByTransactionTimeBetween(Instant from, Instant to);

    @Query("select coalesce(sum(t.amount), 0) from TransactionEntity t "
            + "where t.transactionTime >= :from and t.transactionTime < :to")
    BigDecimal sumAmountBetween(@Param("from") Instant from, @Param("to") Instant to);
}
