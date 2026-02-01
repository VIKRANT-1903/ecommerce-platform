package com.example.searchservice.indexer.service;

import com.example.searchservice.config.SearchIndexProperties;
import com.example.searchservice.indexer.builder.OfferDocumentBuilder;
import com.example.searchservice.indexer.builder.ProductDocumentBuilder;
import com.example.searchservice.indexer.model.OfferRow;
import com.example.searchservice.indexer.model.ProductRow;
import com.example.searchservice.indexer.repository.OfferDataRepository;
import com.example.searchservice.indexer.repository.ProductDataRepository;
import com.example.searchservice.indexer.writer.ElasticBulkWriter;
import com.example.searchservice.model.document.OfferDocument;
import com.example.searchservice.model.document.ProductDocument;
import com.example.searchservice.model.document.SuggestDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * One-time bootstrap job to load initial data into Elasticsearch.
 * 
 * This is critical because CDC alone only captures changes AFTER the indexer starts.
 * Without snapshot loading, the index would be empty or incomplete.
 * 
 * Usage:
 * - Run manually via REST endpoint or CLI command
 * - Or trigger automatically on first startup (with distributed lock)
 * 
 * Safety:
 * - Uses bulk upsert (idempotent, can be rerun)
 * - Does not delete existing docs
 * - Flushes ElasticBulkWriter at the end
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SnapshotLoader {

    private final ProductDataRepository productDataRepository;
    private final OfferDataRepository offerDataRepository;
    
    private final ProductDocumentBuilder productDocumentBuilder;
    private final OfferDocumentBuilder offerDocumentBuilder;
    
    private final ElasticBulkWriter bulkWriter;
    private final SearchIndexProperties indexProperties;

    /**
     * Load all products + offers into Elasticsearch.
     * 
     * This should be run once during initial setup or when rebuilding the index.
     */
    public void loadSnapshot() {
        log.info("Starting snapshot load...");
        
        try {
            loadProducts();
            loadOffers();
            loadSuggestions();
            
            // Ensure all buffered operations are flushed
            bulkWriter.flush();
            
            log.info("Snapshot load completed successfully");
        } catch (Exception ex) {
            log.error("Snapshot load failed", ex);
            throw ex;
        }
    }

    /**
     * Load all active products.
     * 
     * For each product:
     * 1. Fetch product row
     * 2. Fetch related offers
     * 3. Build ProductDocument with aggregates
     * 4. Upsert to Elasticsearch
     */
    private void loadProducts() {
        log.info("Loading products...");
        
        List<ProductRow> products = productDataRepository.findAllActive();
        
        log.info("Found {} active products", products.size());
        
        int successCount = 0;
        int failCount = 0;
        
        for (ProductRow productRow : products) {
            try {
                String productId = productRow.getProductId();
                
                // Fetch related offers
                List<OfferRow> offers = offerDataRepository.findByProductId(productId);
                
                // Build document
                ProductDocument document = productDocumentBuilder.build(productRow, offers);
                
                if (document == null) {
                    log.warn("Skipping product {}: failed to build document", productId);
                    failCount++;
                    continue;
                }
                
                // Upsert to ES
                bulkWriter.upsert(indexProperties.getProduct(), productId, document);
                
                successCount++;
                
                if (successCount % 100 == 0) {
                    log.info("Indexed {} products so far...", successCount);
                }
                
            } catch (Exception ex) {
                log.error("Failed to index product: {}", productRow.getProductId(), ex);
                failCount++;
            }
        }
        
        log.info("Product indexing completed: success={}, failed={}", successCount, failCount);
    }

    /**
     * Load all active offers.
     * 
     * Note: This requires fetching ALL offers, which may not be available
     * from OfferDataRepository (it's designed for per-product queries).
     * 
     * Options:
     * 1. Add a findAllActive() method to OfferDataRepository
     * 2. Iterate through all products and fetch offers per product
     * 3. Skip offer loading if offers are already indexed via product aggregation
     */
    private void loadOffers() {
        log.info("Loading offers...");
        
        List<OfferRow> offers = offerDataRepository.findAllActive();
        
        log.info("Found {} active offers", offers.size());
        
        int successCount = 0;
        int failCount = 0;
        
        for (OfferRow offerRow : offers) {
            try {
                OfferDocument document = offerDocumentBuilder.build(offerRow);
                
                if (document == null) {
                    log.warn("Skipping offer {}: failed to build document", offerRow.getOfferId());
                    failCount++;
                    continue;
                }
                
                bulkWriter.upsert(indexProperties.getOffer(), offerRow.getOfferId(), document);
                successCount++;
                
                if (successCount % 1000 == 0) {
                    log.info("Indexed {} offers so far...", successCount);
                }
                
            } catch (Exception ex) {
                log.error("Failed to index offer: {}", offerRow.getOfferId(), ex);
                failCount++;
            }
        }
        
        log.info("Offer indexing completed: success={}, failed={}", successCount, failCount);
    }
    
    /**
     * Load suggestions from product names.
     * Creates autocomplete suggestions based on product names.
     */
    private void loadSuggestions() {
        log.info("Loading suggestions...");
        
        List<ProductRow> products = productDataRepository.findAllActive();
        
        log.info("Building suggestions from {} products", products.size());
        
        int successCount = 0;
        int failCount = 0;
        
        for (ProductRow productRow : products) {
            try {
                String productName = productRow.getName();
                if (productName == null || productName.isBlank()) {
                    continue;
                }
                
                // Create suggestion from product name
                // Split name into words for better autocomplete
                List<String> inputs = List.of(
                        productName.toLowerCase(),
                        productName
                );
                
                SuggestDocument suggestDoc = SuggestDocument.builder()
                        .input(inputs)
                        .weight((int) (productRow.getPopularityScore() != null ? productRow.getPopularityScore() : 1))
                        .build();
                
                // Use product name as suggestion ID
                String suggestionId = productRow.getProductId() + "_suggest";
                bulkWriter.upsert(indexProperties.getSuggest(), suggestionId, suggestDoc);
                
                successCount++;
                
            } catch (Exception ex) {
                log.error("Failed to create suggestion for product: {}", productRow.getProductId(), ex);
                failCount++;
            }
        }
        
        log.info("Suggestion indexing completed: success={}, failed={}", successCount, failCount);
    }
}
