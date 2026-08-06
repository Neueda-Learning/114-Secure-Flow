package com.neueda.secureflow.alert;

import com.neueda.secureflow.monitoring.RuleType;
import com.neueda.secureflow.transaction.TransactionEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "alerts")
public class AlertEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "rule_type", nullable = false, length = 30)
    private RuleType ruleType;

    @Column(name = "rule_name", nullable = false, length = 100)
    private String ruleName;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private AlertSeverity severity;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 30)
    private AlertStatus status;

    @Column(nullable = false, length = 500)
    private String message;

    @Column(name = "account_id", nullable = false, length = 50)
    private String accountId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "acknowledged_at")
    private Instant acknowledgedAt;

    @Column(name = "investigating_at")
    private Instant investigatingAt;

    @Column(name = "closed_at")
    private Instant closedAt;

    @Column(name = "resolution_notes", length = 500)
    private String resolutionNotes;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "alert_transactions",
            joinColumns = @JoinColumn(name = "alert_id"),
            inverseJoinColumns = @JoinColumn(name = "transaction_id"))
    private Set<TransactionEntity> transactions = new LinkedHashSet<>();

    @OneToMany(mappedBy = "alert", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("changedAt ASC")
    private List<AlertHistoryEntity> history = new ArrayList<>();

    protected AlertEntity() {
    }

    public AlertEntity(RuleType ruleType, String ruleName, AlertSeverity severity,
                       String message, String accountId, Instant createdAt,
                       Collection<TransactionEntity> transactions) {
        this.ruleType = ruleType;
        this.ruleName = ruleName;
        this.severity = severity;
        this.status = AlertStatus.OPEN;
        this.message = message;
        this.accountId = accountId;
        this.createdAt = createdAt;
        this.transactions.addAll(transactions);
        this.history.add(new AlertHistoryEntity(
                this, null, AlertStatus.OPEN, createdAt, "Alert generated automatically"));
    }

    public void changeStatus(AlertStatus nextStatus, String notes, Instant time) {
        AlertStatus oldStatus = status;
        status = nextStatus;

        if (nextStatus == AlertStatus.ACKNOWLEDGED) {
            acknowledgedAt = time;
        }
        if (nextStatus == AlertStatus.INVESTIGATING) {
            investigatingAt = time;
        }
        if (nextStatus == AlertStatus.CLOSED || nextStatus == AlertStatus.DISMISSED) {
            closedAt = time;
            resolutionNotes = notes;
        }

        history.add(new AlertHistoryEntity(this, oldStatus, nextStatus, time, notes));
    }

    public Long getId() { return id; }
    public RuleType getRuleType() { return ruleType; }
    public String getRuleName() { return ruleName; }
    public AlertSeverity getSeverity() { return severity; }
    public AlertStatus getStatus() { return status; }
    public String getMessage() { return message; }
    public String getAccountId() { return accountId; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getAcknowledgedAt() { return acknowledgedAt; }
    public Instant getInvestigatingAt() { return investigatingAt; }
    public Instant getClosedAt() { return closedAt; }
    public String getResolutionNotes() { return resolutionNotes; }
    public Set<TransactionEntity> getTransactions() { return transactions; }
    public List<AlertHistoryEntity> getHistory() { return history; }
}
