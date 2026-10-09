package com.example.paymentsearch.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
@Profile("demo-seed")
public class DemoDataSeeder implements ApplicationRunner {

    private final DataSource dataSource;

    public DemoDataSeeder(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(ApplicationArguments args) {
        ResourceDatabasePopulator populator =
                new ResourceDatabasePopulator(new ClassPathResource("demo-data.sql"));
        populator.execute(dataSource);
    }
}
