package com.chronos.chronos_audit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class ChronosAuditApplication {
	public static void main(String[] args) {
		SpringApplication.run(ChronosAuditApplication.class, args);
	}
}