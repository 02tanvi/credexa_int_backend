package com.app.product.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.app.product.entity.CustomerCommunication;
import com.app.product.entity.Product;

@Repository
public interface CustomerCommunicationRepository extends JpaRepository<CustomerCommunication, Long> {
    
    @Query("SELECT c FROM CustomerCommunication c WHERE c.product.id = :productId")
    List<CustomerCommunication> findByProductId(@Param("productId") Long productId);
    
    @Query("SELECT c FROM CustomerCommunication c WHERE c.product.id = :productId AND c.communicationType = :type")
    List<CustomerCommunication> findByProductIdAndType(@Param("productId") Long productId, @Param("type") String type);
    
    @Query("SELECT c FROM CustomerCommunication c WHERE c.product.id = :productId AND c.event = :event")
    List<CustomerCommunication> findByProductIdAndEvent(@Param("productId") Long productId, @Param("event") String event);
    
    long countByProduct(Product product);
    
}
