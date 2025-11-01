package com.app.product.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    // Cache names
    public static final String PRODUCTS_CACHE = "products";
    public static final String PRODUCTS_BY_CODE_CACHE = "productsByCode";
    public static final String PRODUCTS_BY_TYPE_CACHE = "productsByType";
    public static final String ACTIVE_PRODUCTS_CACHE = "activeProducts";
    public static final String PRICING_CACHE = "pricing";
    public static final String INTEREST_RATES_CACHE = "interestRates";

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
                PRODUCTS_CACHE,
                PRODUCTS_BY_CODE_CACHE,
                PRODUCTS_BY_TYPE_CACHE,
                ACTIVE_PRODUCTS_CACHE,
                PRICING_CACHE,
                INTEREST_RATES_CACHE
        );
        
        cacheManager.setCaffeine(caffeineCacheBuilder());
        return cacheManager;
    }

    private Caffeine<Object, Object> caffeineCacheBuilder() {
        return Caffeine.newBuilder()
                .initialCapacity(100)
                .maximumSize(1000)
                .expireAfterWrite(60, TimeUnit.MINUTES)
                .expireAfterAccess(30, TimeUnit.MINUTES)
                .recordStats();
    }
}