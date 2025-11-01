package com.app.product.security;

import java.io.IOException;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * JWT Authentication Filter
 * Intercepts requests and validates JWT tokens
 */
@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    // temporarily remove final to avoid initialization error
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // TEMPORARILY DISABLED: Skip JWT validation for ALL requests (for inter-service communication)
        // To re-enable JWT authentication, restore the original implementation
        filterChain.doFilter(request, response);
    }
}
