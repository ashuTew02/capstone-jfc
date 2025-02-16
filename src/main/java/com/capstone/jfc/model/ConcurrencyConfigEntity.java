package com.capstone.jfc.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "concurrency_config")
public class ConcurrencyConfigEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Indicates what type of concurrency limit this row specifies:
     *  - "TYPE_TOOL" for (eventType, tool) concurrency
     *  - "TENANT" for tenant-based concurrency
     */
    @Column(name = "concurrency_type", nullable = false)
    private String concurrencyType;  // e.g., "TYPE_TOOL" or "TENANT"

    @Column(name = "event_type")
    private String eventType;        // used if concurrencyType = "TYPE_TOOL"

    @Column(name = "tool")
    private String tool;             // used if concurrencyType = "TYPE_TOOL"

    @Column(name = "tenant_id")
    private Long tenantId;           // used if concurrencyType = "TENANT"

    @Column(name = "concurrency_limit", nullable = false)
    private Integer concurrencyLimit;

    @Column(name = "created_at", nullable = false, columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false, columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;

    public ConcurrencyConfigEntity() {
    }

    @PrePersist
    public void onPrePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    public void onPreUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // -- Getters/Setters --

    public Long getId() {
        return id;
    }

    public String getConcurrencyType() {
        return concurrencyType;
    }

    public void setConcurrencyType(String concurrencyType) {
        this.concurrencyType = concurrencyType;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getTool() {
        return tool;
    }

    public void setTool(String tool) {
        this.tool = tool;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public Integer getConcurrencyLimit() {
        return concurrencyLimit;
    }

    public void setConcurrencyLimit(Integer concurrencyLimit) {
        this.concurrencyLimit = concurrencyLimit;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
