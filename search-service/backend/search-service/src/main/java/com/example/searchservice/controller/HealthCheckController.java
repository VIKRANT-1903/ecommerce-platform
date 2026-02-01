package com.example.searchservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Health check controller to verify database connections.
 */
@Slf4j
@RestController
@RequestMapping("/api/health")
@RequiredArgsConstructor
public class HealthCheckController {

    private final MongoTemplate mongoTemplate;
    private final JdbcTemplate jdbcTemplate;

    @GetMapping("/databases")
    public ResponseEntity<Map<String, Object>> checkDatabases() {
        Map<String, Object> status = new HashMap<>();
        
        // Check MongoDB (Product DB)
        try {
            mongoTemplate.getDb().getName();
            long productCount = mongoTemplate.getCollection("products").countDocuments();
            status.put("mongodb", Map.of(
                    "status", "UP",
                    "database", mongoTemplate.getDb().getName(),
                    "productCount", productCount
            ));
        } catch (Exception ex) {
            log.error("MongoDB connection failed", ex);
            status.put("mongodb", Map.of(
                    "status", "DOWN",
                    "error", ex.getMessage()
            ));
        }
        
        // Check PostgreSQL (Offer DB)
        try {
            Long offerCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM offers", 
                    Long.class
            );
            Long inventoryCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM inventory", 
                    Long.class
            );
            status.put("postgresql", Map.of(
                    "status", "UP",
                    "offerCount", offerCount,
                    "inventoryCount", inventoryCount
            ));
        } catch (Exception ex) {
            log.error("PostgreSQL connection failed", ex);
            status.put("postgresql", Map.of(
                    "status", "DOWN",
                    "error", ex.getMessage()
            ));
        }
        
        boolean allHealthy = status.values().stream()
                .allMatch(v -> v instanceof Map && "UP".equals(((Map<?, ?>) v).get("status")));
        
        return allHealthy 
                ? ResponseEntity.ok(status)
                : ResponseEntity.status(503).body(status);
    }
}
