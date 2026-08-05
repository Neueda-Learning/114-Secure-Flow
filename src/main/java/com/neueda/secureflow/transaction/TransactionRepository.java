package com.neueda.secureflow.transaction;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TransactionRepository extends JpaRepository<TransactionEntity, Long> {
    boolean existsByAccountIdAndPayeeId(String accountId, String payeeId);

    List<TransactionEntity> findByAccountIdAndTransactionTimeBetweenOrderByTransactionTimeAsc(
            String accountId, Instant from, Instant to);

    @Query("""
            select t from TransactionEntity t
            where (:search is null
                or lower(t.accountId) like lower(concat('%', :search, '%'))
                or lower(t.payeeId) like lower(concat('%', :search, '%'))
                or lower(coalesce(t.description, '')) like lower(concat('%', :search, '%')))
              and (:minAmount is null or t.amount >= :minAmount)
              and (:maxAmount is null or t.amount <= :maxAmount)
              and (:fromTime is null or t.transactionTime >= :fromTime)
              and (:toTime is null or t.transactionTime <= :toTime)
            """)
    Page<TransactionEntity> search(
            @Param("search") String search,
            @Param("minAmount") BigDecimal minAmount,
            @Param("maxAmount") BigDecimal maxAmount,
            @Param("fromTime") Instant from,
            @Param("toTime") Instant to,
            Pageable pageable);

    long countByTransactionTimeBetween(Instant from, Instant to);

    @Query("""
            select coalesce(sum(t.amount), 0)
            from TransactionEntity t
            where t.transactionTime >= :from and t.transactionTime < :to
            """)
    BigDecimal sumBetween(@Param("from") Instant from, @Param("to") Instant to);
}
