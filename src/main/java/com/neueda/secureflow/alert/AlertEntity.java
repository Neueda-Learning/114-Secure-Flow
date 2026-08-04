package com.neueda.secureflow.alert;

import com.neueda.secureflow.monitoring.RuleMatch;
import com.neueda.secureflow.monitoring.RuleType;
import com.neueda.secureflow.transaction.TransactionEntity;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "alerts")
public class AlertEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING) @Column(name = "rule_type", nullable = false)
    private RuleType ruleType;
    @Column(name = "rule_name", nullable = false)
    private String ruleName;
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private AlertSeverity severity;
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private AlertStatus status;
    @Column(nullable = false, length = 500)
    private String message;
    @Column(name = "account_id", nullable = false)
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
    @ManyToMany
    @JoinTable(name = "alert_transactions", joinColumns = @JoinColumn(name = "alert_id"),
            inverseJoinColumns = @JoinColumn(name = "transaction_id"))
    private Set<TransactionEntity> triggeringTransactions = new LinkedHashSet<>();
    @OneToMany(mappedBy = "alert", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("changedAt ASC")
    private List<AlertStatusHistoryEntity> history = new ArrayList<>();

    protected AlertEntity() { }

    public AlertEntity(RuleMatch match, Instant now) {
        this.ruleType = match.ruleType();
        this.ruleName = match.ruleName();
        this.severity = match.severity();
        this.status = AlertStatus.OPEN;
        this.message = match.message();
        this.accountId = match.accountId();
        this.createdAt = now;
        this.triggeringTransactions.addAll(match.transactions());
        this.history.add(new AlertStatusHistoryEntity(this, null, AlertStatus.OPEN, now, "Alert created"));
    }

    public void transition(AlertStatus next, Instant now, String notes) {
        AlertStatus previous = status;
        status = next;
        if (next == AlertStatus.ACKNOWLEDGED) acknowledgedAt = now;
        if (next == AlertStatus.INVESTIGATING) investigatingAt = now;
        if (next == AlertStatus.CLOSED || next == AlertStatus.DISMISSED) {
            closedAt = now;
            resolutionNotes = notes;
        }
        history.add(new AlertStatusHistoryEntity(this, previous, next, now, notes));
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
    public Set<TransactionEntity> getTriggeringTransactions() { return triggeringTransactions; }
    public List<AlertStatusHistoryEntity> getHistory() { return history; }
}
