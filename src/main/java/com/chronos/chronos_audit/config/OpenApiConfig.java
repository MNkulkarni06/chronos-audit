package com.chronos.chronos_audit.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Chronos-Audit: Subscription Leak Engine API")
                        .version("1.0.0")
                        .description("Automated backend ledger framework that evaluates subscription risk matrices based on normalized email, network, and financial telemetry signals.")
                        .contact(new Contact()
                                .name("Backend Engineering Team")
                                .email("developer@chronos.io")));
    }
}