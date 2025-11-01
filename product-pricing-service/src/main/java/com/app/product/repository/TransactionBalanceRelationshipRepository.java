package com.app.product.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.app.product.entity.TransactionBalanceRelationship;
import com.app.product.enums.BalanceType;
import com.app.product.enums.TransactionType;

@Repository
public interface TransactionBalanceRelationshipRepository extends JpaRepository<TransactionBalanceRelationship, Long> {
    
    List<TransactionBalanceRelationship> findByTransactionType(TransactionType transactionType);
    
    List<TransactionBalanceRelationship> findByBalanceType(BalanceType balanceType);
    
    @Query("SELECT r FROM TransactionBalanceRelationship r WHERE r.transactionType = :txnType AND r.balanceType = :balType")
    Optional<TransactionBalanceRelationship> findByTransactionTypeAndBalanceType(
            @Param("txnType") TransactionType transactionType, 
            @Param("balType") BalanceType balanceType);
    
    @Query("SELECT r FROM TransactionBalanceRelationship r WHERE r.active = true")
    List<TransactionBalanceRelationship> findAllActive();
}
