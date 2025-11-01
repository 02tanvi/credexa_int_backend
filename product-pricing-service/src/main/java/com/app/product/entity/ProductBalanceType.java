package com.app.product.entity;

import com.app.product.enums.BalanceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;

@Entity
@Table(
    name = "product_balance_types",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_product_balance_type", 
        columnNames = {"product_id", "balance_type"}
    )
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductBalanceType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(name = "balance_type", nullable = false, length = 50)
    private BalanceType balanceType;

    @Column(name = "description", length = 500)
    private String description;

    @Builder.Default
    @Column(name = "tracked", nullable = false)
    private Boolean tracked = true;

    // ✅ Add this method to ensure default value before persisting
    @PrePersist
    public void prePersist() {
        if (this.tracked == null) {
            this.tracked = true;
        }
    }
}