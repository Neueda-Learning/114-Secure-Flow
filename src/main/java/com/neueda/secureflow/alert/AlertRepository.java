package com.neueda.secureflow.alert;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AlertRepository extends JpaRepository<AlertEntity, Long>,
        JpaSpecificationExecutor<AlertEntity> {
}
