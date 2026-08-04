CREATE INDEX idx_transactions_account_payee_time
    ON transactions (account_id, payee_id, transaction_time);

CREATE INDEX idx_alerts_status_created
    ON alerts (status, created_at);

CREATE INDEX idx_alerts_account_created
    ON alerts (account_id, created_at);

CREATE INDEX idx_alert_history_alert_time
    ON alert_status_history (alert_id, changed_at);
