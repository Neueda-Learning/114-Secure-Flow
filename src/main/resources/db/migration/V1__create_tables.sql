CREATE TABLE transactions (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    account_id VARCHAR(50) NOT NULL,
    payee_id VARCHAR(50) NOT NULL,
    amount DECIMAL(19,2) NOT NULL,
    currency CHAR(3) NOT NULL,
    transaction_time DATETIME(6) NOT NULL,
    description VARCHAR(255),
    created_at DATETIME(6) NOT NULL
);

CREATE TABLE alerts (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
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
    resolution_notes VARCHAR(500)
);

CREATE TABLE alert_transactions (
    alert_id BIGINT NOT NULL,
    transaction_id BIGINT NOT NULL,
    PRIMARY KEY (alert_id, transaction_id),
    FOREIGN KEY (alert_id) REFERENCES alerts(id),
    FOREIGN KEY (transaction_id) REFERENCES transactions(id)
);

CREATE TABLE alert_status_history (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    alert_id BIGINT NOT NULL,
    previous_status VARCHAR(30),
    new_status VARCHAR(30) NOT NULL,
    changed_at DATETIME(6) NOT NULL,
    note VARCHAR(500),
    FOREIGN KEY (alert_id) REFERENCES alerts(id)
);

CREATE INDEX idx_transactions_account_time ON transactions (account_id, transaction_time);
CREATE INDEX idx_transactions_account_payee ON transactions (account_id, payee_id);
CREATE INDEX idx_alerts_status ON alerts (status);
