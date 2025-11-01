package com.app.calculator.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

/**
 * Swagger/OpenAPI configuration
 */
@Configuration
public class SwaggerConfig {
    
    @Bean
    public OpenAPI fdCalculatorOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("FD Calculator Service API")
                .description("Fixed Deposit Calculator and Simulator - Calculate maturity amounts, compare scenarios, and get detailed breakdowns")
                .version("v1.0.0")
                .contact(new Contact()
                    .name("Credexa Support")
                    .email("support@credexa.com"))
                .license(new License()
                    .name("Apache 2.0")
                    .url("https://www.apache.org/licenses/LICENSE-2.0")))
            .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
            .components(new Components()
                .addSecuritySchemes("Bearer Authentication", 
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("Enter JWT token obtained from login-service")));
    }
}
