package com.app.fdaccount;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main application class for FD Account Service
 * Manages Fixed Deposit accounts with full lifecycle support
 */
@SpringBootApplication(scanBasePackages = {"com.app.fdaccount", "com.app.common"})
@EnableCaching
@EnableScheduling
public class FdAccountApplication {

    public static void main(String[] args) {
        SpringApplication.run(FdAccountApplication.class, args);
        System.out.println("\n" +
                "==============================================\n" +
                "🏦 FD Account Service started successfully!\n" +
                "📊 Swagger UI: http://localhost:8086/api/fd-accounts/swagger-ui.html\n" +
                "💰 Managing Fixed Deposit Accounts\n" +
                "==============================================\n");
    }
}
