package com.almaakcorp.entreprise.audit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.UUID;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
    Page<AuditLog> findByUsernameIgnoreCaseOrderByCreatedAtDesc(String username, Pageable pageable);
    Page<AuditLog> findByActionIgnoreCaseOrderByCreatedAtDesc(String action, Pageable pageable);
    Page<AuditLog> findByUsernameIgnoreCaseAndActionIgnoreCaseAndCreatedAtBetweenOrderByCreatedAtDesc(String username, String action, Instant from, Instant to, Pageable pageable);
}
