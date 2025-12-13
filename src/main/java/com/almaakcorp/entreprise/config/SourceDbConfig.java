package com.almaakcorp.entreprise.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableJpaRepositories(
        basePackages = "com.almaakcorp.entreprise.migration.source",
        entityManagerFactoryRef = "sourceEntityManagerFactory",
        transactionManagerRef = "sourceTransactionManager"
)
public class SourceDbConfig {

    @Value("${spring.datasource.source.url:jdbc:mysql://localhost:3306/almaakcorp_quotation_db}")
    private String url;
    @Value("${spring.datasource.source.username:root}")
    private String username;
    @Value("${spring.datasource.source.password:your_new_password}")
    private String password;
    @Value("${spring.datasource.source.driver-class-name:com.mysql.cj.jdbc.Driver}")
    private String driver;

    @Bean(name = "sourceDataSource")
    public DataSource sourceDataSource() {
        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setDriverClassName(driver);
        ds.setUrl(url);
        ds.setUsername(username);
        ds.setPassword(password);
        return ds;
    }

    @Bean(name = "sourceEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean sourceEntityManagerFactory(
            EntityManagerFactoryBuilder builder) {
        Map<String, Object> props = new HashMap<>();
        props.put("hibernate.hbm2ddl.auto", "none");
        return builder
                .dataSource(sourceDataSource())
                .packages("com.almaakcorp.entreprise.models")
                .persistenceUnit("source")
                .properties(props)
                .build();
    }

    @Bean(name = "sourceTransactionManager")
    public PlatformTransactionManager sourceTransactionManager(LocalContainerEntityManagerFactoryBean sourceEntityManagerFactory) {
        return new JpaTransactionManager(sourceEntityManagerFactory.getObject());
    }
}
