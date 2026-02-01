package com.example.searchservice.indexer.repository;

import com.example.searchservice.indexer.model.ProductRow;

import java.util.List;
import java.util.Optional;

/**
 * Repository for fetching product data during snapshot loading.
 * 
 * Implementation will depend on your data source:
 * - Direct DB access via JDBC/JPA
 * - REST call to product service
 * - Batch file exports
 */
public interface ProductDataRepository {

    /**
     * Fetch all active products (for snapshot bootstrap).
     */
    List<ProductRow> findAllActive();

    /**
     * Fetch a single product by ID.
     */
    Optional<ProductRow> findById(String productId);
}
