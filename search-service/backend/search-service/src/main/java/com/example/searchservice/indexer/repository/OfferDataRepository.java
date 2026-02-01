package com.example.searchservice.indexer.repository;

import com.example.searchservice.indexer.model.OfferRow;

import java.util.List;

/**
 * Repository for fetching offer data needed during product indexing.
 * 
 * Implementation will depend on your data source:
 * - Direct DB access via JDBC/JPA
 * - REST call to offer service
 * - Cache/snapshot table
 */
public interface OfferDataRepository {

    /**
     * Fetch all active offers for a given product.
     * Used to compute product-level aggregates (minPrice, maxPrice, merchantCount).
     */
    List<OfferRow> findByProductId(String productId);
    
    /**
     * Fetch all active offers (for snapshot loading).
     */
    List<OfferRow> findAllActive();
}
