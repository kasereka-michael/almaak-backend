package com.almaakcorp.entreprise.config;

import com.almaakcorp.entreprise.models.Role;
import com.almaakcorp.entreprise.models.User;
import com.almaakcorp.entreprise.repositories.RoleRepository;
import com.almaakcorp.entreprise.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@Slf4j
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Ensure base roles exist
        Role roleAdmin = roleRepository.findByName("ROLE_ADMIN").orElseGet(() ->
                roleRepository.save(Role.builder().name("ROLE_ADMIN").build())
        );
        Role roleUser = roleRepository.findByName("ROLE_USER").orElseGet(() ->
                roleRepository.save(Role.builder().name("ROLE_USER").build())
        );

        if (!userRepository.existsByUsername("kaserekamichael23526@gmail.com")) {
            User admin = User.builder()
                    .username("kaserekamichael23526@gmail.com")
                    .email("kaserekamichael23526@gmail.com")
                    .password(passwordEncoder.encode("kaserekamichael23526"))
                    .enabled(true)
                    .accountNonExpired(true)
                    .accountNonLocked(true)
                    .credentialsNonExpired(true)
                    .roles(Set.of(roleAdmin))
                    .build();
            userRepository.save(admin);
            log.info("Seeded default admin user 'michael'.");
        } else {
            log.info("Default admin user 'michael' already exists.");
        }
    }
}