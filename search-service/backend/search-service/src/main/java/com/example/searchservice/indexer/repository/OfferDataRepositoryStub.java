package com.example.searchservice.indexer.repository;

import com.example.searchservice.indexer.model.OfferRow;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

/**
 * Stub implementation for OfferDataRepository.
 * 
 * TODO: Replace with actual implementation:
 * - JdbcTemplate query to offers table
 * - RestTemplate call to offer-service
 * - Query from snapshot/materialized view
 */
@Slf4j
@Repository
public class OfferDataRepositoryStub implements OfferDataRepository {

    @Override
    public List<OfferRow> findByProductId(String productId) {
        log.warn("STUB: OfferDataRepository.findByProductId({})", productId);
        
        // TODO: Implement actual query
        // Example JDBC:
        // return jdbcTemplate.query(
        //     "SELECT * FROM offers WHERE product_id = ?",
        //     new Object[]{productId},
        //     new BeanPropertyRowMapper<>(OfferRow.class)
        // );
        
        return Collections.emptyList();
    }
    
    @Override
    public List<OfferRow> findAllActive() {
        log.warn("STUB: OfferDataRepository.findAllActive()");
        return Collections.emptyList();
    }
}
