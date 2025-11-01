package com.app.product.config;

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
public class OpenApiConfig {

    @Bean
    public OpenAPI productPricingOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Product and Pricing Service API")
                        .description("Comprehensive API for managing banking products, pricing, interest rates, charges, and business rules")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Credexa Team")
                                .email("support@credexa.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter JWT token obtained from login service")));
    }
}
