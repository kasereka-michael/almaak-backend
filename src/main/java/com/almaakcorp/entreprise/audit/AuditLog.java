package com.almaakcorp.entreprise.audit;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "audit_logs", indexes = {
        @Index(name = "idx_audit_created_at", columnList = "created_at"),
        @Index(name = "idx_audit_username_created_at", columnList = "username,created_at"),
        @Index(name = "idx_audit_action_created_at", columnList = "action,created_at")
})
public class AuditLog {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "username", length = 150)
    private String username;

    @Column(name = "user_id", length = 64)
    private String userId;

    @Column(name = "action", length = 64, nullable = false)
    private String action;

    @Column(name = "entity", length = 128)
    private String entity;

    @Column(name = "entity_id", length = 128)
    private String entityId;

    @Column(name = "http_method", length = 16)
    private String httpMethod;

    @Column(name = "path", length = 512)
    private String path;

    @Column(name = "ip", length = 64)
    private String ip;

    @Column(name = "details", columnDefinition = "TEXT")
    private String details;

    @Column(name = "success", nullable = false)
    private boolean success;

    @Column(name = "error", length = 1024)
    private String error;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}
