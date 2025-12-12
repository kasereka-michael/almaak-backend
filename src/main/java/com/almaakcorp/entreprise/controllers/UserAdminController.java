package com.almaakcorp.entreprise.controllers;

import com.almaakcorp.entreprise.models.Role;
import com.almaakcorp.entreprise.models.User;
import com.almaakcorp.entreprise.repositories.RoleRepository;
import com.almaakcorp.entreprise.repositories.UserRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserAdminController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    public List<User> list() {
        return userRepository.findAll();
    }

    // Get current authenticated user's profile
    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> getProfile(Authentication auth) {
        if (auth == null || auth.getName() == null) return ResponseEntity.status(401).build();
        Optional<User> opt = userRepository.findByUsername(auth.getName());
        if (opt.isEmpty()) return ResponseEntity.status(404).build();
        return ResponseEntity.ok(UserProfileResponse.from(opt.get()));
    }

    // Update current authenticated user's profile
    @PutMapping("/profile")
    public ResponseEntity<UserProfileResponse> updateProfile(Authentication auth, @RequestBody UpdateOwnProfileRequest req) {
        if (auth == null || auth.getName() == null) return ResponseEntity.status(401).build();
        Optional<User> opt = userRepository.findByUsername(auth.getName());
        if (opt.isEmpty()) return ResponseEntity.status(404).build();
        User user = opt.get();
        if (req.getFirstName() != null) user.setFirstName(req.getFirstName());
        if (req.getLastName() != null) user.setLastName(req.getLastName());
        if (req.getEmail() != null && !req.getEmail().isBlank()) user.setEmail(req.getEmail());
        if (req.getUsername() != null && !req.getUsername().isBlank()) user.setUsername(req.getUsername());
        if (req.getPhone() != null) user.setPhone(req.getPhone());
        if (req.getDepartment() != null) user.setDepartment(req.getDepartment());
        if (req.getPosition() != null) user.setPosition(req.getPosition());
        if (req.getBio() != null) user.setBio(req.getBio());
        User saved = userRepository.save(user);
        return ResponseEntity.ok(UserProfileResponse.from(saved));
    }

    @PostMapping
    public ResponseEntity<User> create(@RequestBody CreateUserRequest req) {
        if (req.getEmail() == null || req.getEmail().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        if (userRepository.existsByUsername(req.getUsername()) || userRepository.existsByEmail(req.getEmail())) {
            return ResponseEntity.status(409).build();
        }
        Set<Role> roles = new HashSet<>();
        if (req.getRoles() != null && !req.getRoles().isEmpty()) {
            for (String name : req.getRoles()) {
                String normalized = name.startsWith("ROLE_") ? name : "ROLE_" + name;
                Role role = roleRepository.findByName(normalized)
                        .orElseGet(() -> roleRepository.save(Role.builder().name(normalized).build()));
                roles.add(role);
            }
        } else {
            Role roleUser = roleRepository.findByName("ROLE_USER")
                    .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_USER").build()));
            roles.add(roleUser);
        }
        User user = User.builder()
                .username(req.getUsername())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .roles(roles)
                .build();
        return ResponseEntity.ok(userRepository.save(user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> update(@PathVariable Long id, @RequestBody UpdateUserRequest req) {
        Optional<User> opt = userRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        User user = opt.get();
        if (req.getPassword() != null && !req.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(req.getPassword()));
        }
        if (req.getEnabled() != null) {
            user.setEnabled(req.getEnabled());
        }
        if (req.getAccountNonExpired() != null) {
            user.setAccountNonExpired(req.getAccountNonExpired());
        }
        if (req.getAccountNonLocked() != null) {
            user.setAccountNonLocked(req.getAccountNonLocked());
        }
        if (req.getCredentialsNonExpired() != null) {
            user.setCredentialsNonExpired(req.getCredentialsNonExpired());
        }
        if (req.getEmail() != null && !req.getEmail().isBlank()) {
            user.setEmail(req.getEmail());
        }
        if (req.getRoles() != null) {
            Set<Role> roles = new HashSet<>();
            for (String name : req.getRoles()) {
                String normalized = name.startsWith("ROLE_") ? name : "ROLE_" + name;
                Role role = roleRepository.findByName(normalized)
                        .orElseGet(() -> roleRepository.save(Role.builder().name(normalized).build()));
                roles.add(role);
            }
            user.setRoles(roles);
        }
        return ResponseEntity.ok(userRepository.save(user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!userRepository.existsById(id)) return ResponseEntity.notFound().build();
        userRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // Optional helper: get user by id
    @GetMapping("/{id}")
    public ResponseEntity<User> getById(@PathVariable Long id) {
        return userRepository.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @Data
    public static class CreateUserRequest {
        private String username;
        private String email;
        private String password;
        private List<String> roles;
    }

    @Data
    public static class UpdateUserRequest {
        private String password;
        private String email;
        private Boolean enabled;
        private Boolean accountNonExpired;
        private Boolean accountNonLocked;
        private Boolean credentialsNonExpired;
        private List<String> roles;
    }

    @Data
    public static class UpdateOwnProfileRequest {
        private String firstName;
        private String lastName;
        private String email;
        private String username;
        private String phone;
        private String department;
        private String position;
        private String bio;
    }

    @Data
    public static class UserProfileResponse {
        private Long id;
        private String username;
        private String email;
        private String firstName;
        private String lastName;
        private String phone;
        private String department;
        private String position;
        private String bio;
        private List<String> roles;

        public static UserProfileResponse from(User u) {
            UserProfileResponse r = new UserProfileResponse();
            r.id = u.getId();
            r.username = u.getUsername();
            r.email = u.getEmail();
            r.firstName = u.getFirstName();
            r.lastName = u.getLastName();
            r.phone = u.getPhone();
            r.department = u.getDepartment();
            r.position = u.getPosition();
            r.bio = u.getBio();
            r.roles = u.getRoles().stream().map(Role::getName)
                    .map(name -> name.startsWith("ROLE_") ? name.substring(5) : name)
                    .toList();
            return r;
        }
    }
}
