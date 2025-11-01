package com.app.login.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Role entity for user authorization
 */
@Entity
@Table(name = "roles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    @Enumerated(EnumType.STRING)
    private RoleName name;

    @Column(length = 500)
    private String description;

    public enum RoleName {
        ROLE_ADMIN,
        ROLE_USER,
        ROLE_CUSTOMER_MANAGER,
        ROLE_PRODUCT_MANAGER,
        ROLE_FD_MANAGER,
        ROLE_REPORT_VIEWER
    }
}
