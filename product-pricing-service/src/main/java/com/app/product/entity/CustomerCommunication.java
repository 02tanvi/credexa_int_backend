package com.app.product.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Customer Communication Configuration Entity
 * Defines communication preferences and templates for product events
 */
@Entity
@Table(name = "customer_communications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerCommunication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false, length = 50)
    private String communicationType;  // EMAIL, SMS, PUSH, LETTER

    @Column(nullable = false, length = 100)
    private String event;  // ACCOUNT_OPENING, MATURITY_REMINDER, INTEREST_CREDIT, etc.

    @Column(length = 100)
    private String template;  // Template name/ID

    @Column(length = 200)
    private String subject;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column
    @Builder.Default
    private Boolean mandatory = false;

    @Column
    @Builder.Default
    private Boolean active = true;
}
