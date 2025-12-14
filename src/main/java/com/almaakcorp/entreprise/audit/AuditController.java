package com.almaakcorp.entreprise.audit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/audit")
public class AuditController {

    private final AuditLogRepository repo;

    public AuditController(AuditLogRepository repo) { this.repo = repo; }

    @GetMapping("/logs")
    public Page<AuditLog> logs(@RequestParam(required = false) String username,
                               @RequestParam(required = false) String action,
                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        if (username != null && action != null && from != null && to != null) {
            return repo.findByUsernameIgnoreCaseAndActionIgnoreCaseAndCreatedAtBetweenOrderByCreatedAtDesc(username, action, from, to, pageable);
        }
        if (username != null) return repo.findByUsernameIgnoreCaseOrderByCreatedAtDesc(username, pageable);
        if (action != null) return repo.findByActionIgnoreCaseOrderByCreatedAtDesc(action, pageable);
        return repo.findAll(pageable);
    }
}
