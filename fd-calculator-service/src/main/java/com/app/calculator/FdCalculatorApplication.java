package com.app.calculator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main application class for FD Calculator Service
 * Provides calculation and simulation capabilities for Fixed Deposits
 */
@SpringBootApplication(scanBasePackages = {"com.app.calculator", "com.app.common"})
@EnableCaching
@EnableScheduling
public class FdCalculatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(FdCalculatorApplication.class, args);
        System.out.println("🧮 FD Calculator Service started successfully on port 8085!");
        System.out.println("📊 Swagger UI: http://localhost:8085/api/calculator/swagger-ui.html");
    }
}
