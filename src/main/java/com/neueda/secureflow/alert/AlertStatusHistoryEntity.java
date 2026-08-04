package com.neueda.secureflow.alert;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "alert_status_history")
public class AlertStatusHistoryEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "alert_id", nullable = false)
    private AlertEntity alert;
    @Enumerated(EnumType.STRING) @Column(name = "previous_status")
    private AlertStatus previousStatus;
    @Enumerated(EnumType.STRING) @Column(name = "new_status", nullable = false)
    private AlertStatus newStatus;
    @Column(name = "changed_at", nullable = false)
    private Instant changedAt;
    @Column(length = 500)
    private String note;

    protected AlertStatusHistoryEntity() { }

    AlertStatusHistoryEntity(AlertEntity alert, AlertStatus previousStatus, AlertStatus newStatus,
                             Instant changedAt, String note) {
        this.alert = alert;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.changedAt = changedAt;
        this.note = note;
    }

    public AlertStatus getPreviousStatus() { return previousStatus; }
    public AlertStatus getNewStatus() { return newStatus; }
    public Instant getChangedAt() { return changedAt; }
    public String getNote() { return note; }
}
