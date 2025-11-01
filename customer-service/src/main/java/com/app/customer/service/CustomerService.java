package com.app.customer.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.customer.client.EmailServiceClient;
import com.app.customer.client.LoginServiceClient;
import com.app.customer.dto.CreateCustomerRequest;
import com.app.customer.dto.Customer360Response;
import com.app.customer.dto.CustomerClassificationResponse;
import com.app.customer.dto.CustomerResponse;
import com.app.customer.dto.UpdateCustomerRequest;
import com.app.customer.entity.Customer;
import com.app.customer.exception.CustomerNotFoundException;
import com.app.customer.exception.DuplicateCustomerException;
import com.app.customer.repository.CustomerRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for customer operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final LoginServiceClient loginServiceClient;
    private final EmailServiceClient emailServiceClient;

    /**
     * Create a new customer
     */
    @Transactional
    public CustomerResponse createCustomer(CreateCustomerRequest request, String authenticatedUsername,
            boolean isAdmin) {
        log.info("Creating customer by user: {} (Admin: {})", authenticatedUsername, isAdmin);

        // Get userId from login-service using the authenticated username
        Long userId = loginServiceClient.getUserIdByUsername(authenticatedUsername);
        log.info("Retrieved userId: {} for username: {}", userId, authenticatedUsername);

        // Security Check: Regular users can only create their own profile
        // Check if the authenticated user already has a customer profile
        if (!isAdmin && customerRepository.existsByUsername(authenticatedUsername)) {
            throw new DuplicateCustomerException(
                    "You already have a customer profile. Each user can only have one profile.");
        }

        // Check for duplicates (using the userId from login-service, not from request)
        if (customerRepository.existsByUserId(userId)) {
            throw new DuplicateCustomerException("Customer profile already exists for this user");
        }
        validateDuplicateCustomer(request);

        // Auto-determine classification based on age if SENIOR_CITIZEN or SUPER_SENIOR
        Customer.CustomerClassification classification = determineClassification(
                request.getDateOfBirth(),
                request.getClassification());

        // Build customer entity - userId from login-service, username from JWT
        Customer customer = Customer.builder()
                .userId(userId) // Use userId from login-service, not from request
                .username(authenticatedUsername) // Store the authenticated username from JWT
                .fullName(request.getFullName())
                .mobileNumber(request.getMobileNumber())
                .email(request.getEmail())
                .panNumber(request.getPanNumber())
                .aadharNumber(request.getAadharNumber())
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender())
                .classification(classification)
                .kycStatus(Customer.KycStatus.PENDING)
                .addressLine1(request.getAddressLine1())
                .addressLine2(request.getAddressLine2())
                .city(request.getCity())
                .state(request.getState())
                .pincode(request.getPincode())
                .country(request.getCountry())
                .accountNumber(request.getAccountNumber())
                .ifscCode(request.getIfscCode())
                .preferredLanguage(request.getPreferredLanguage() != null ? request.getPreferredLanguage() : "en")
                .preferredCurrency(request.getPreferredCurrency() != null ? request.getPreferredCurrency() : "INR")
                .emailNotifications(request.getEmailNotifications() != null ? request.getEmailNotifications() : true)
                .smsNotifications(request.getSmsNotifications() != null ? request.getSmsNotifications() : true)
                .build();

        Customer savedCustomer = customerRepository.save(customer);
        log.info("Customer created successfully with ID: {}", savedCustomer.getId());

        // Send new customer email asynchronously (non-blocking)
        try {
            // Format the registration date
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
            String registeredDate = savedCustomer.getCreatedAt().format(formatter);

            emailServiceClient.sendNewCustomerEmail(
                    savedCustomer.getEmail(),
                    savedCustomer.getFullName(),
                    savedCustomer.getId().toString(),
                    savedCustomer.getMobileNumber(),
                    registeredDate);
            log.info("New customer email triggered for: {}", savedCustomer.getEmail());
        } catch (Exception e) {
            log.error("Failed to trigger new customer email, but customer creation succeeded", e);
            // Don't fail the customer creation if email fails
        }

        return CustomerResponse.fromEntity(savedCustomer);
    }

    /**
     * Get customer by ID
     */
    @Transactional(readOnly = true)
    public CustomerResponse getCustomerById(Long id) {
        log.info("Fetching customer by ID: {}", id);
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + id));
        return CustomerResponse.fromEntity(customer);
    }

    /**
     * Get customer by user ID
     */
    @Transactional(readOnly = true)
    public CustomerResponse getCustomerByUserId(Long userId) {
        log.info("Fetching customer by user ID: {}", userId);
        Customer customer = customerRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found for user ID: " + userId));
        return CustomerResponse.fromEntity(customer);
    }

    /**
     * Update customer
     */
    @Transactional
    public CustomerResponse updateCustomer(Long id, UpdateCustomerRequest request, String authenticatedUsername,
            boolean isAdmin) {
        log.info("Updating customer with ID: {} by user: {} (Admin: {})", id, authenticatedUsername, isAdmin);

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + id));

        // Security Check: Regular users can only update their own profile
        // Admin users can update any profile
        if (!isAdmin && !customer.getUsername().equals(authenticatedUsername)) {
            throw new com.app.customer.exception.UnauthorizedAccessException(
                    "You can only update your own customer profile. Admin access required to update other profiles.");
        }

        log.debug("User '{}' (Admin: {}) authorized to update customer for userId: {}",
                authenticatedUsername, isAdmin, customer.getUserId());

        // Track updated fields for email notification
        StringBuilder updatedFields = new StringBuilder();

        // Update only non-null fields and track changes
        if (request.getFullName() != null && !request.getFullName().equals(customer.getFullName())) {
            updatedFields.append("✓ Full Name: ").append(customer.getFullName())
                    .append(" → ").append(request.getFullName()).append("\n");
            customer.setFullName(request.getFullName());
        }
        if (request.getMobileNumber() != null && !request.getMobileNumber().equals(customer.getMobileNumber())) {
            // Check if mobile number already exists for another customer
            customerRepository.findByMobileNumber(request.getMobileNumber())
                    .ifPresent(existing -> {
                        if (!existing.getId().equals(id)) {
                            throw new DuplicateCustomerException("Mobile number already registered");
                        }
                    });
            updatedFields.append("✓ Mobile Number: ").append(customer.getMobileNumber())
                    .append(" → ").append(request.getMobileNumber()).append("\n");
            customer.setMobileNumber(request.getMobileNumber());
        }
        if (request.getEmail() != null && !request.getEmail().equals(customer.getEmail())) {
            // Check if email already exists for another customer
            customerRepository.findByEmail(request.getEmail())
                    .ifPresent(existing -> {
                        if (!existing.getId().equals(id)) {
                            throw new DuplicateCustomerException("Email already registered");
                        }
                    });
            updatedFields.append("✓ Email: ").append(customer.getEmail())
                    .append(" → ").append(request.getEmail()).append("\n");
            customer.setEmail(request.getEmail());
        }
        if (request.getPanNumber() != null && !request.getPanNumber().equals(customer.getPanNumber())) {
            updatedFields.append("✓ PAN Number: ").append(customer.getPanNumber())
                    .append(" → ").append(request.getPanNumber()).append("\n");
            customer.setPanNumber(request.getPanNumber());
        }
        if (request.getAadharNumber() != null && !request.getAadharNumber().equals(customer.getAadharNumber())) {
            updatedFields.append("✓ Aadhar Number: ").append(customer.getAadharNumber())
                    .append(" → ").append(request.getAadharNumber()).append("\n");
            customer.setAadharNumber(request.getAadharNumber());
        }
        if (request.getDateOfBirth() != null && !request.getDateOfBirth().equals(customer.getDateOfBirth())) {
            updatedFields.append("✓ Date of Birth: ").append(customer.getDateOfBirth())
                    .append(" → ").append(request.getDateOfBirth()).append("\n");
            customer.setDateOfBirth(request.getDateOfBirth());
            // Re-determine classification based on new age
            if (customer.getClassification() == Customer.CustomerClassification.SENIOR_CITIZEN ||
                    customer.getClassification() == Customer.CustomerClassification.SUPER_SENIOR) {
                customer.setClassification(determineClassification(
                        request.getDateOfBirth(),
                        customer.getClassification()));
            }
        }
        if (request.getGender() != null && !request.getGender().equals(customer.getGender())) {
            updatedFields.append("✓ Gender: ").append(customer.getGender())
                    .append(" → ").append(request.getGender()).append("\n");
            customer.setGender(request.getGender());
        }
        if (request.getClassification() != null && !request.getClassification().equals(customer.getClassification())) {
            updatedFields.append("✓ Classification: ").append(customer.getClassification())
                    .append(" → ").append(request.getClassification()).append("\n");
            customer.setClassification(request.getClassification());
        }
        if (request.getKycStatus() != null && !request.getKycStatus().equals(customer.getKycStatus())) {
            updatedFields.append("✓ KYC Status: ").append(customer.getKycStatus())
                    .append(" → ").append(request.getKycStatus()).append("\n");
            customer.setKycStatus(request.getKycStatus());
        }
        if (request.getAddressLine1() != null && !request.getAddressLine1().equals(customer.getAddressLine1())) {
            updatedFields.append("✓ Address Line 1: ").append(customer.getAddressLine1())
                    .append(" → ").append(request.getAddressLine1()).append("\n");
            customer.setAddressLine1(request.getAddressLine1());
        }
        if (request.getAddressLine2() != null && !request.getAddressLine2().equals(customer.getAddressLine2())) {
            updatedFields.append("✓ Address Line 2: ").append(customer.getAddressLine2())
                    .append(" → ").append(request.getAddressLine2()).append("\n");
            customer.setAddressLine2(request.getAddressLine2());
        }
        if (request.getCity() != null && !request.getCity().equals(customer.getCity())) {
            updatedFields.append("✓ City: ").append(customer.getCity())
                    .append(" → ").append(request.getCity()).append("\n");
            customer.setCity(request.getCity());
        }
        if (request.getState() != null && !request.getState().equals(customer.getState())) {
            updatedFields.append("✓ State: ").append(customer.getState())
                    .append(" → ").append(request.getState()).append("\n");
            customer.setState(request.getState());
        }
        if (request.getPincode() != null && !request.getPincode().equals(customer.getPincode())) {
            updatedFields.append("✓ Pincode: ").append(customer.getPincode())
                    .append(" → ").append(request.getPincode()).append("\n");
            customer.setPincode(request.getPincode());
        }
        if (request.getCountry() != null && !request.getCountry().equals(customer.getCountry())) {
            updatedFields.append("✓ Country: ").append(customer.getCountry())
                    .append(" → ").append(request.getCountry()).append("\n");
            customer.setCountry(request.getCountry());
        }
        if (request.getIsActive() != null && !request.getIsActive().equals(customer.getIsActive())) {
            updatedFields.append("✓ Active Status: ").append(customer.getIsActive())
                    .append(" → ").append(request.getIsActive()).append("\n");
            customer.setIsActive(request.getIsActive());
        }
        if (request.getAccountNumber() != null && !request.getAccountNumber().equals(customer.getAccountNumber())) {
            updatedFields.append("✓ Account Number: ").append(customer.getAccountNumber())
                    .append(" → ").append(request.getAccountNumber()).append("\n");
            customer.setAccountNumber(request.getAccountNumber());
        }
        if (request.getIfscCode() != null && !request.getIfscCode().equals(customer.getIfscCode())) {
            updatedFields.append("✓ IFSC Code: ").append(customer.getIfscCode())
                    .append(" → ").append(request.getIfscCode()).append("\n");
            customer.setIfscCode(request.getIfscCode());
        }
        if (request.getPreferredLanguage() != null
                && !request.getPreferredLanguage().equals(customer.getPreferredLanguage())) {
            updatedFields.append("✓ Preferred Language: ").append(customer.getPreferredLanguage())
                    .append(" → ").append(request.getPreferredLanguage()).append("\n");
            customer.setPreferredLanguage(request.getPreferredLanguage());
        }
        if (request.getPreferredCurrency() != null
                && !request.getPreferredCurrency().equals(customer.getPreferredCurrency())) {
            updatedFields.append("✓ Preferred Currency: ").append(customer.getPreferredCurrency())
                    .append(" → ").append(request.getPreferredCurrency()).append("\n");
            customer.setPreferredCurrency(request.getPreferredCurrency());
        }
        if (request.getEmailNotifications() != null
                && !request.getEmailNotifications().equals(customer.getEmailNotifications())) {
            updatedFields.append("✓ Email Notifications: ").append(customer.getEmailNotifications())
                    .append(" → ").append(request.getEmailNotifications()).append("\n");
            customer.setEmailNotifications(request.getEmailNotifications());
        }
        if (request.getSmsNotifications() != null
                && !request.getSmsNotifications().equals(customer.getSmsNotifications())) {
            updatedFields.append("✓ SMS Notifications: ").append(customer.getSmsNotifications())
                    .append(" → ").append(request.getSmsNotifications()).append("\n");
            customer.setSmsNotifications(request.getSmsNotifications());
        }

        Customer updatedCustomer = customerRepository.save(customer);
        log.info("Customer updated successfully with ID: {}", updatedCustomer.getId());

        // Send email notification if any fields were updated
        if (updatedFields.length() > 0) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");
                String updateDate = LocalDateTime.now().format(formatter);
                emailServiceClient.sendCustomerUpdateEmail(
                        updatedCustomer.getEmail(),
                        updatedCustomer.getFullName(),
                        updatedFields.toString(),
                        updateDate);
                log.info("Update notification email sent to: {}", updatedCustomer.getEmail());
            } catch (Exception e) {
                log.error("Failed to send update notification email to: {}", updatedCustomer.getEmail(), e);
            }
        }

        return CustomerResponse.fromEntity(updatedCustomer);
    }

    /**
     * Get customer classification (for FD rate determination)
     */
    @Transactional(readOnly = true)
    public CustomerClassificationResponse getCustomerClassification(Long id) {
        log.info("Fetching customer classification for ID: {}", id);
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + id));
        return CustomerClassificationResponse.fromCustomer(customer);
    }

    /**
     * Get 360-degree customer view
     */
    @Transactional(readOnly = true)
    public Customer360Response getCustomer360View(Long id) {
        log.info("Fetching 360-degree view for customer ID: {}", id);

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + id));

        CustomerResponse customerInfo = CustomerResponse.fromEntity(customer);
        CustomerClassificationResponse classificationInfo = CustomerClassificationResponse.fromCustomer(customer);

        // TODO: Fetch FD accounts from FD service when available
        // For now, return empty account summary
        Customer360Response.AccountSummary accountSummary = Customer360Response.AccountSummary.builder()
                .totalFdAccounts(0)
                .activeFdAccounts(0)
                .maturedFdAccounts(0)
                .closedFdAccounts(0)
                .totalInvestedAmount(BigDecimal.ZERO)
                .totalMaturityAmount(BigDecimal.ZERO)
                .totalInterestEarned(BigDecimal.ZERO)
                .build();

        return Customer360Response.builder()
                .customerInfo(customerInfo)
                .classificationInfo(classificationInfo)
                .accountSummary(accountSummary)
                .fdAccounts(null) // Will be populated from FD service
                .build();
    }

    /**
     * Validate duplicate customer (checking mobile, email, PAN, Aadhar)
     * Note: userId is already checked in createCustomer method
     */
    private void validateDuplicateCustomer(CreateCustomerRequest request) {
        if (customerRepository.existsByMobileNumber(request.getMobileNumber())) {
            throw new DuplicateCustomerException("Mobile number already registered");
        }
        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateCustomerException("Email already registered");
        }
        if (request.getPanNumber() != null && customerRepository.existsByPanNumber(request.getPanNumber())) {
            throw new DuplicateCustomerException("PAN number already registered");
        }
        if (request.getAadharNumber() != null && customerRepository.existsByAadharNumber(request.getAadharNumber())) {
            throw new DuplicateCustomerException("Aadhar number already registered");
        }
    }

    /**
     * Determine customer classification based on age
     */
    private Customer.CustomerClassification determineClassification(
            LocalDate dateOfBirth,
            Customer.CustomerClassification requestedClassification) {

        int age = Period.between(dateOfBirth, LocalDate.now()).getYears();

        log.debug("Determining classification - DOB: {}, Current Age: {}, Requested Classification: {}",
                dateOfBirth, age, requestedClassification);

        // Auto-determine senior citizen classifications based on age
        if (age >= 80) {
            log.info("Auto-classifying customer as SUPER_SENIOR (age: {})", age);
            return Customer.CustomerClassification.SUPER_SENIOR;
        } else if (age >= 60) {
            log.info("Auto-classifying customer as SENIOR_CITIZEN (age: {})", age);
            return Customer.CustomerClassification.SENIOR_CITIZEN;
        }

        // For non-senior citizens, use requested classification
        log.debug("Using requested classification: {} (age: {})", requestedClassification, age);
        return requestedClassification;
    }
}
