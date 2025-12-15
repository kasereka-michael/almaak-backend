package com.almaakcorp.entreprise.controllers;

import com.almaakcorp.entreprise.models.TrashItem;
import com.almaakcorp.entreprise.service_interface.service_interface_implel.TrashImplementation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/trash/v1")
@RequiredArgsConstructor
public class TrashController {

    private final TrashImplementation trashService;

    @GetMapping("/items")
    public ResponseEntity<?> getItems(@RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "20") int size,
                                      @RequestParam(required = false) String username,
                                      @RequestParam(required = false, defaultValue = "false") boolean admin) {
        try {
            String user = (username != null && !username.isBlank()) ? username : "demo_user";
            List<TrashItem> items = trashService.getTrashItems(user, admin, page, size);
            return ResponseEntity.ok(items);
        } catch (Exception e) {
            log.error("Error listing trash items", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/items/{trashId}")
    public ResponseEntity<?> getItem(@PathVariable String trashId) {
        return trashService.getTrashItemById(trashId)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PostMapping("/items/{trashId}/restore")
    public ResponseEntity<?> restore(@PathVariable String trashId,
                                     @RequestParam(required = false) String username,
                                     @RequestParam(required = false, defaultValue = "false") boolean admin) {
        try {
            String user = (username != null && !username.isBlank()) ? username : "demo_user";
            Optional<Map<String, Object>> restored = trashService.restoreFromTrash(trashId, user, admin);
            if (restored.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            // Note: domain-specific re-insert is handled by resource-specific restore endpoints where available
            return ResponseEntity.ok(Map.of("message", "Restored from trash", "data", restored.get()));
        } catch (Exception e) {
            log.error("Error restoring trash item", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/items/{trashId}")
    public ResponseEntity<?> deletePermanently(@PathVariable String trashId,
                                               @RequestParam(required = false, defaultValue = "false") boolean admin) {
        try {
            boolean removed = trashService.permanentlyDelete(trashId, admin);
            if (!removed) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Not allowed or item not found"));
            }
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error permanently deleting trash item", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/clear")
    public ResponseEntity<?> clearTrash(@RequestParam(defaultValue = "false") boolean clearAll,
                                        @RequestParam(required = false) String username,
                                        @RequestParam(required = false, defaultValue = "false") boolean admin) {
        try {
            if (clearAll) {
                boolean ok = trashService.clearAllTrash(admin);
                if (!ok) return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Admin only"));
                return ResponseEntity.noContent().build();
            } else {
                String user = (username != null && !username.isBlank()) ? username : "demo_user";
                trashService.clearUserTrash(user);
                return ResponseEntity.noContent().build();
            }
        } catch (Exception e) {
            log.error("Error clearing trash", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<?> stats(@RequestParam(required = false) String username,
                                   @RequestParam(required = false, defaultValue = "false") boolean admin) {
        try {
            String user = (username != null && !username.isBlank()) ? username : "demo_user";
            Map<String, Object> stats = trashService.getTrashStatistics(user, admin);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error getting trash stats", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/size")
    public ResponseEntity<?> size() {
        try {
            long current = trashService.getCurrentTrashSize();
            long threshold = trashService.getTrashSizeThreshold();
            return ResponseEntity.ok(Map.of(
                    "currentSize", current,
                    "threshold", threshold,
                    "exceeded", current > threshold
            ));
        } catch (Exception e) {
            log.error("Error getting trash size", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }
}
