package com.app.login.repository;

import com.app.login.entity.BankConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BankConfigurationRepository extends JpaRepository<BankConfiguration, Long> {
    
    Optional<BankConfiguration> findByActiveTrue();
}
