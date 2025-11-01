package com.app.fdaccount.batch;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.app.fdaccount.dto.external.CustomerDto;
import com.app.fdaccount.entity.AccountRole;
import com.app.fdaccount.entity.FdAccount;
import com.app.fdaccount.enums.AccountStatus;
import com.app.fdaccount.repository.FdAccountRepository;
import com.app.fdaccount.service.integration.CustomerServiceClient;
import com.app.fdaccount.service.integration.EmailServiceClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Batch job for sending maturity notices
 * Runs at 2:00 AM daily (after maturity processing)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MaturityNoticeBatch {

    private final FdAccountRepository accountRepository;
    private final CustomerServiceClient customerServiceClient;
    private final EmailServiceClient emailServiceClient;

    @Value("${batch.maturity-notice.days-before:10}")
    private int daysBeforeMaturity;

    @Value("${alert.sms.enabled:true}")
    private boolean smsEnabled;

    @Value("${alert.email.enabled:true}")
    private boolean emailEnabled;

    /**
     * Send maturity notices for accounts maturing in N days
     * Scheduled to run at 2:00 AM daily
     */
    @Scheduled(cron = "${batch.maturity-notice.cron:0 0 2 * * ?}")
    public void sendMaturityNotices() {
        log.info("🕐 Starting maturity notice batch...");

        LocalDate today = LocalDate.now();
        LocalDate noticeDate = today.plusDays(daysBeforeMaturity);
        long startTime = System.currentTimeMillis();

        // Get accounts maturing on the notice date
        List<FdAccount> upcomingMaturityAccounts = accountRepository.findByMaturityDateAndStatus(
                noticeDate, AccountStatus.ACTIVE);

        log.info("Found {} accounts maturing on {} ({} days from today)",
                upcomingMaturityAccounts.size(), noticeDate, daysBeforeMaturity);

        int successCount = 0;
        int errorCount = 0;

        for (FdAccount account : upcomingMaturityAccounts) {
            try {
                sendMaturityNotice(account);
                successCount++;
            } catch (Exception e) {
                log.error("❌ Error sending maturity notice for account: {}",
                        account.getAccountNumber(), e);
                errorCount++;
            }
        }

        long duration = System.currentTimeMillis() - startTime;

        log.info("✅ Maturity notice batch completed in {}ms - Success: {}, Errors: {}",
                duration, successCount, errorCount);
    }

    /**
     * Send maturity notice for a single account
     */
    private void sendMaturityNotice(FdAccount account) {
        log.info("Sending maturity notice for account: {}", account.getAccountNumber());

        // Get primary owner
        AccountRole primaryOwner = account.getRoles().stream()
                .filter(role -> Boolean.TRUE.equals(role.getIsPrimary()) &&
                        Boolean.TRUE.equals(role.getIsActive()))
                .findFirst()
                .orElse(account.getRoles().stream()
                        .filter(AccountRole::getIsActive)
                        .findFirst()
                        .orElse(null));

        if (primaryOwner == null) {
            log.warn("No active owner found for account: {}", account.getAccountNumber());
            return;
        }

        // Send SMS if enabled
        if (smsEnabled) {
            sendSMS(primaryOwner.getCustomerId(), primaryOwner.getCustomerName(), account);
        }

        // Send Email if enabled
        if (emailEnabled) {
            sendEmail(primaryOwner.getCustomerId(), primaryOwner.getCustomerName(), account);
        }

        log.info("✅ Sent maturity notice to customer {} for account: {}",
                primaryOwner.getCustomerId(), account.getAccountNumber());
    }

    /**
     * Send SMS notification (mock implementation)
     */
    private void sendSMS(Long customerId, String customerName, FdAccount account) {
        // In production, this would integrate with SMS gateway
        log.info("📱 SMS notification for FD maturity would be sent to customer {} ({})",
                customerId, customerName);

        // Mock SMS sending
        // smsGateway.send(customerPhone, message);
    }

    /**
     * Send Email notification using templated email
     */
    private void sendEmail(Long customerId, String customerName, FdAccount account) {
        try {
            // Fetch customer details to get email address
            CustomerDto customer = customerServiceClient.getCustomerById(customerId);

            if (customer == null || customer.getEmail() == null || customer.getEmail().isEmpty()) {
                log.warn("No email address found for customer: {}", customerId);
                return;
            }

            // Format data for email template
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
            LocalDate today = LocalDate.now();
            long daysUntil = java.time.temporal.ChronoUnit.DAYS.between(today, account.getMaturityDate());

            String principalAmount = String.format("₹ %,.2f", account.getPrincipalAmount());
            String maturityAmount = String.format("₹ %,.2f", account.getMaturityAmount());
            String interestRate = String.format("%.2f%%",
                    account.getCustomInterestRate() != null ? account.getCustomInterestRate()
                            : account.getInterestRate());
            String maturityDate = account.getMaturityDate().format(formatter);
            String daysUntilMaturity = daysUntil + " days";
            String maturityInstruction = account.getMaturityInstruction() != null
                    ? account.getMaturityInstruction().toString()
                    : "HOLD";
            String transferAccount = account.getMaturityTransferAccount() != null ? account.getMaturityTransferAccount()
                    : "";

            // Send email using the templated email service
            emailServiceClient.sendFdMaturityNotificationEmail(
                    customer.getEmail(),
                    customer.getCustomerName(),
                    account.getAccountNumber(),
                    account.getAccountName(),
                    principalAmount,
                    interestRate,
                    maturityDate,
                    maturityAmount,
                    daysUntilMaturity,
                    maturityInstruction,
                    transferAccount);

            log.info("✅ FD maturity notification email sent to customer {} ({})",
                    customerId, customer.getEmail());
        } catch (Exception e) {
            log.error("❌ Failed to send email to customer {}: {}", customerId, e.getMessage(), e);
        }
    }
}
