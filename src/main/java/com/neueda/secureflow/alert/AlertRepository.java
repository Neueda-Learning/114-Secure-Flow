package com.neueda.secureflow.alert;

import java.util.Collection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AlertRepository extends JpaRepository<AlertEntity, Long> {
    @Query("""
            select a from AlertEntity a
            where (:status is null or a.status = :status)
              and (:severity is null or a.severity = :severity)
            """)
    Page<AlertEntity> search(
            @Param("status") AlertStatus status,
            @Param("severity") AlertSeverity severity,
            Pageable pageable);

    long countByStatusIn(Collection<AlertStatus> statuses);
}
