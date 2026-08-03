CREATE TABLE transactions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    account_id VARCHAR(50) NOT NULL,
    payee_id VARCHAR(50) NOT NULL,
    amount DECIMAL(19,2) NOT NULL,
    currency CHAR(3) NOT NULL,
    transaction_time DATETIME(6) NOT NULL,
    description VARCHAR(255),
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_transactions_account_time (account_id, transaction_time),
    INDEX idx_transactions_account_payee (account_id, payee_id)
);
