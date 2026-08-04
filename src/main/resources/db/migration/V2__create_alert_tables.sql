CREATE TABLE alerts (
    id BIGINT NOT NULL AUTO_INCREMENT,
    rule_type VARCHAR(30) NOT NULL,
    rule_name VARCHAR(100) NOT NULL,
    severity VARCHAR(20) NOT NULL,
    status VARCHAR(30) NOT NULL,
    message VARCHAR(500) NOT NULL,
    account_id VARCHAR(50) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    acknowledged_at DATETIME(6),
    investigating_at DATETIME(6),
    closed_at DATETIME(6),
    resolution_notes VARCHAR(500),
    PRIMARY KEY (id)
);

CREATE TABLE alert_transactions (
    alert_id BIGINT NOT NULL,
    transaction_id BIGINT NOT NULL,
    PRIMARY KEY (alert_id, transaction_id),
    CONSTRAINT fk_alert_transaction_alert FOREIGN KEY (alert_id) REFERENCES alerts(id),
    CONSTRAINT fk_alert_transaction_transaction FOREIGN KEY (transaction_id) REFERENCES transactions(id)
);

CREATE TABLE alert_status_history (
    id BIGINT NOT NULL AUTO_INCREMENT,
    alert_id BIGINT NOT NULL,
    previous_status VARCHAR(30),
    new_status VARCHAR(30) NOT NULL,
    changed_at DATETIME(6) NOT NULL,
    note VARCHAR(500),
    PRIMARY KEY (id),
    CONSTRAINT fk_alert_history_alert FOREIGN KEY (alert_id) REFERENCES alerts(id)
);
