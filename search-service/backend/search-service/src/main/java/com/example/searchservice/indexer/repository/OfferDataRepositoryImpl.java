package com.example.searchservice.indexer.repository;

import com.example.searchservice.indexer.model.OfferRow;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;

/**
 * PostgreSQL implementation for OfferDataRepository.
 * 
 * Queries the 'offers' table in ecommerce_auth database.
 */
@Slf4j
@Repository
@Primary
@RequiredArgsConstructor
public class OfferDataRepositoryImpl implements OfferDataRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<OfferRow> findByProductId(String productId) {
        log.debug("Fetching offers for productId: {}", productId);
        
        String sql = """
                SELECT 
                    offer_id,
                    product_id,
                    merchant_id,
                    price,
                    currency,
                    merchant_rating,
                    status,
                    created_at,
                    updated_at
                FROM offers
                WHERE product_id = ?
                AND status = 'ACTIVE'
                """;
        
        try {
            List<OfferRow> offers = jdbcTemplate.query(
                    sql,
                    new Object[]{productId},
                    new OfferRowMapper()
            );
            
            log.debug("Found {} offers for product {}", offers.size(), productId);
            return offers;
            
        } catch (Exception ex) {
            log.error("Failed to fetch offers for product {}", productId, ex);
            throw new RuntimeException("Failed to fetch offers", ex);
        }
    }
    
    @Override
    public List<OfferRow> findAllActive() {
        log.debug("Fetching all active offers");
        
        String sql = """
                SELECT 
                    offer_id,
                    product_id,
                    merchant_id,
                    price,
                    currency,
                    merchant_rating,
                    status,
                    created_at,
                    updated_at
                FROM offers
                WHERE status = 'ACTIVE'
                """;
        
        try {
            List<OfferRow> offers = jdbcTemplate.query(
                    sql,
                    new OfferRowMapper()
            );
            
            log.info("Found {} active offers", offers.size());
            return offers;
            
        } catch (Exception ex) {
            log.error("Failed to fetch all active offers", ex);
            throw new RuntimeException("Failed to fetch offers", ex);
        }
    }

    /**
     * Row mapper for OfferRow.
     * Maps PostgreSQL result set to OfferRow object.
     */
    private static class OfferRowMapper implements RowMapper<OfferRow> {
        
        @Override
        public OfferRow mapRow(ResultSet rs, int rowNum) throws SQLException {
            return OfferRow.builder()
                    .offerId(String.valueOf(rs.getLong("offer_id")))
                    .productId(rs.getString("product_id"))
                    .merchantId(String.valueOf(rs.getLong("merchant_id")))
                    .price(rs.getDouble("price"))
                    .currency(rs.getString("currency"))
                    .availableQty(0) // Not in DB, default to 0
                    .merchantRating(rs.getDouble("merchant_rating"))
                    .productRating(0.0) // Not in DB, default to 0
                    .merchantSalesVolume(0L) // Not in DB, default to 0
                    .merchantCatalogSize(0) // Not in DB, default to 0
                    .offerStatus(rs.getString("status"))
                    .createdAt(getInstant(rs, "created_at"))
                    .updatedAt(getInstant(rs, "updated_at"))
                    .build();
        }
        
        private Instant getInstant(ResultSet rs, String columnName) throws SQLException {
            var timestamp = rs.getTimestamp(columnName);
            return timestamp != null ? timestamp.toInstant() : null;
        }
    }
}
