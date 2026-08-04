package com.neueda.secureflow.alert;

import java.time.Instant;
import java.util.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AlertRepository extends JpaRepository<AlertEntity, Long>, JpaSpecificationExecutor<AlertEntity> {
    long countByStatusIn(Collection<AlertStatus> statuses);
    long countByCreatedAtBetween(Instant from, Instant to);
}
