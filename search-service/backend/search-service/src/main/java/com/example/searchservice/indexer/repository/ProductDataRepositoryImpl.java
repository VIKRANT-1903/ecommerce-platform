package com.example.searchservice.indexer.repository;

import com.example.searchservice.indexer.model.ProductRow;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * MongoDB implementation for ProductDataRepository.
 * 
 * Connects to product_db and queries the 'products' collection.
 */
@Slf4j
@Repository
@Primary
@RequiredArgsConstructor
public class ProductDataRepositoryImpl implements ProductDataRepository {

    private final MongoTemplate mongoTemplate;
    
    private static final String COLLECTION_NAME = "products";

    @Override
    public List<ProductRow> findAllActive() {
        log.debug("Fetching all active products from MongoDB");
        
        try {
            // Fetch all products that are not DELETED
            // Include: ACTIVE, OUT_OF_STOCK, INACTIVE
            Query query = new Query(
                    Criteria.where("status").ne("DELETED")
            );
            
            List<ProductRow> products = mongoTemplate.find(
                    query, 
                    ProductRow.class, 
                    COLLECTION_NAME
            );
            
            log.info("Found {} products (excluding DELETED)", products.size());
            return products;
            
        } catch (Exception ex) {
            log.error("Failed to fetch products from MongoDB", ex);
            throw new RuntimeException("Failed to fetch products", ex);
        }
    }

    @Override
    public Optional<ProductRow> findById(String productId) {
        log.debug("Fetching product by id: {}", productId);
        
        try {
            Query query = new Query(Criteria.where("productId").is(productId));
            
            ProductRow product = mongoTemplate.findOne(
                    query, 
                    ProductRow.class, 
                    COLLECTION_NAME
            );
            
            return Optional.ofNullable(product);
            
        } catch (Exception ex) {
            log.error("Failed to fetch product {} from MongoDB", productId, ex);
            return Optional.empty();
        }
    }
}
