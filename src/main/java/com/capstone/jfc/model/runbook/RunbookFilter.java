package com.capstone.jfc.model.runbook;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import com.capstone.jfc.model.FindingSeverity;
import com.capstone.jfc.model.FindingState;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "runbook_filters")
public class RunbookFilter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // For example, if the user chooses to filter by severity=HIGH or state=OPEN
    @Enumerated(EnumType.STRING)
    private FindingState state;
    
    @Enumerated(EnumType.STRING)
    private FindingSeverity severity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "runbook_id", nullable = false)
    @JsonIgnore
    private Runbook runbook;

    @Column(name = "created_at", nullable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public FindingState getState() { return state; }
    public void setState(FindingState state) { this.state = state; }

    public FindingSeverity getSeverity() { return severity; }
    public void setSeverity(FindingSeverity severity) { this.severity = severity; }

    public Runbook getRunbook() { return runbook; }
    public void setRunbook(Runbook runbook) { this.runbook = runbook; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
