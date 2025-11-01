package com.app.product.entity;

import com.app.product.enums.RoleType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
 * Product Role Entity
 * Defines allowed roles for a product
 */
@Entity
@Table(name = "product_roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private RoleType roleType;

    @Column
    @Builder.Default
    private Boolean mandatory = false;

    @Column
    @Builder.Default
    private Integer minCount = 0;

    @Column
    private Integer maxCount;

    @Column(length = 500)
    private String description;
}
