package com.almaakcorp.entreprise;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableJpaAuditing
@EntityScan(basePackages = {
		"com.almaakcorp.entreprise.models",
		"com.almaakcorp.entreprise.audit"
})
@EnableJpaRepositories(basePackages = {
		"com.almaakcorp.entreprise.repositories",
		"com.almaakcorp.entreprise.audit"
})
public class AlmaakcorpEntrepriseManagementSystemApplication {
	public static void main(String[] args) {
		SpringApplication.run(AlmaakcorpEntrepriseManagementSystemApplication.class, args);
	}

}
