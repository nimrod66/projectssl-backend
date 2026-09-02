package com.starnet.SslAgency;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@SpringBootApplication
@EnableJpaAuditing
@EnableJpaRepositories
@EntityScan
@EnableMethodSecurity
@EnableAsync
public class SslAgencyApplication {

	public static void main(String[] args) {
		SpringApplication.run(SslAgencyApplication.class, args);
	}
}
