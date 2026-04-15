package com.locmns;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class LocMnsBackApplication {

    public static void main(String[] args) {
        SpringApplication.run(LocMnsBackApplication.class, args);
    }

}
