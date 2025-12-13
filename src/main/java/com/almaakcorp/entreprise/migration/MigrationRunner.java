package com.almaakcorp.entreprise.migration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MigrationRunner implements CommandLineRunner {

    private final MigrationService migrationService;

    @Value("${app.migration.enabled:false}")
    private boolean enabled;

    @Override
    public void run(String... args) throws Exception {
        if (!enabled) {
            log.info("Migration disabled. Set app.migration.enabled=true to run.");
            return;
        }
        migrationService.migrateAll();
    }
}
