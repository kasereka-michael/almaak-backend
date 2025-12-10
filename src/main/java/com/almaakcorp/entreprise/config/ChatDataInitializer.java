package com.almaakcorp.entreprise.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatDataInitializer implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {
        // Chat module disabled in this build; skip initialization
    }
}