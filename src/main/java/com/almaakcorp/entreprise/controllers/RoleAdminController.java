package com.almaakcorp.entreprise.controllers;

import com.almaakcorp.entreprise.models.Role;
import com.almaakcorp.entreprise.repositories.RoleRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleAdminController {

    private final RoleRepository roleRepository;

    @GetMapping
    public List<Role> list() {
        return roleRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Role> get(@PathVariable Long id) {
        return roleRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Role> create(@RequestBody CreateRoleRequest req) {
        if (req.getName() == null || req.getName().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        String normalized = req.getName().startsWith("ROLE_") ? req.getName() : "ROLE_" + req.getName();
        if (roleRepository.existsByName(normalized)) {
            return ResponseEntity.status(409).build();
        }
        Role role = Role.builder().name(normalized).build();
        return ResponseEntity.ok(roleRepository.save(role));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Role> update(@PathVariable Long id, @RequestBody UpdateRoleRequest req) {
        Optional<Role> opt = roleRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        Role role = opt.get();
        if (req.getName() != null && !req.getName().isBlank()) {
            String normalized = req.getName().startsWith("ROLE_") ? req.getName() : "ROLE_" + req.getName();
            // If changing to a name that exists and different id -> conflict
            Optional<Role> existing = roleRepository.findByName(normalized);
            if (existing.isPresent() && !existing.get().getId().equals(id)) {
                return ResponseEntity.status(409).build();
            }
            role.setName(normalized);
        }
        return ResponseEntity.ok(roleRepository.save(role));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!roleRepository.existsById(id)) return ResponseEntity.notFound().build();
        roleRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Data
    public static class CreateRoleRequest {
        private String name;
    }

    @Data
    public static class UpdateRoleRequest {
        private String name;
    }
}
