package com.neueda.secureflow.alert;

import org.springframework.data.jpa.domain.Specification;

final class AlertSpecifications {
    private AlertSpecifications() {}

    static Specification<AlertEntity> withFilters(AlertStatus status, AlertSeverity severity) {
        Specification<AlertEntity> result = Specification.unrestricted();
        if (status != null) {
            result = result.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }
        if (severity != null) {
            result = result.and((root, query, cb) -> cb.equal(root.get("severity"), severity));
        }
        return result;
    }
}
