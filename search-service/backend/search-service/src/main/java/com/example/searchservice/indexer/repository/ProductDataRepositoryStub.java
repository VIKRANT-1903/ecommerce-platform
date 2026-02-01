package com.example.searchservice.indexer.repository;

import com.example.searchservice.indexer.model.ProductRow;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Stub implementation for ProductDataRepository.
 * 
 * TODO: Replace with actual implementation:
 * - JdbcTemplate query to products table
 * - RestTemplate call to product-service
 * - Read from batch export files
 */
@Slf4j
@Repository
public class ProductDataRepositoryStub implements ProductDataRepository {

    @Override
    public List<ProductRow> findAllActive() {
        log.warn("STUB: ProductDataRepository.findAllActive()");
        
        // TODO: Implement actual query
        // Example JDBC:
        // return jdbcTemplate.query(
        //     "SELECT * FROM products WHERE status = 'ACTIVE'",
        //     new BeanPropertyRowMapper<>(ProductRow.class)
        // );
        
        return Collections.emptyList();
    }

    @Override
    public Optional<ProductRow> findById(String productId) {
        log.warn("STUB: ProductDataRepository.findById({})", productId);
        
        // TODO: Implement actual query
        
        return Optional.empty();
    }
}
