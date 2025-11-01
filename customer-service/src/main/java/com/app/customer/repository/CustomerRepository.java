package com.app.customer.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.app.customer.entity.Customer;

/**
 * Repository for Customer entity
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    /**
     * Find customer by user ID
     */
    Optional<Customer> findByUserId(Long userId);

    /**
     * Find customer by username
     */
    Optional<Customer> findByUsername(String username);

    /**
     * Find customer by mobile number
     */
    Optional<Customer> findByMobileNumber(String mobileNumber);

    /**
     * Find customer by email
     */
    Optional<Customer> findByEmail(String email);

    /**
     * Check if customer exists by user ID
     */
    boolean existsByUserId(Long userId);

    /**
     * Check if customer exists by username
     */
    boolean existsByUsername(String username);

    /**
     * Check if customer exists by mobile number
     */
    boolean existsByMobileNumber(String mobileNumber);

    /**
     * Check if customer exists by email
     */
    boolean existsByEmail(String email);

    /**
     * Check if customer exists by PAN number
     */
    boolean existsByPanNumber(String panNumber);

    /**
     * Check if customer exists by Aadhar number
     */
    boolean existsByAadharNumber(String aadharNumber);
}
