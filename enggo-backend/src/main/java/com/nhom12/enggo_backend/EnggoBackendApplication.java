package com.nhom12.enggo_backend;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.nhom12.enggo_backend.repository")
@EnableScheduling
public class EnggoBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(EnggoBackendApplication.class, args);
	}

}
