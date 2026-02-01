package com.example.searchservice.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

/**
 * MongoDB configuration for accessing product/offer/inventory databases.
 * 
 * If you have separate DBs:
 * - mongodb.product.uri=mongodb://localhost:27017/product_db
 * - mongodb.offer.uri=mongodb://localhost:27017/offer_db
 * - mongodb.inventory.uri=mongodb://localhost:27017/inventory_db
 * 
 * If you have one DB with multiple collections:
 * - spring.data.mongodb.uri=mongodb://localhost:27017/ecommerce_db
 */
@Configuration
public class MongoDbConfig {

    @Value("${spring.data.mongodb.uri:mongodb://localhost:27017/ecommerce_db}")
    private String mongoUri;

    // Option A: Single DB, multiple collections (simpler)
    @Bean
    public MongoTemplate mongoTemplate() {
        MongoClient mongoClient = MongoClients.create(mongoUri);
        String databaseName = extractDatabaseName(mongoUri);
        return new MongoTemplate(mongoClient, databaseName);
    }

    // Option B: If you need separate DBs, uncomment and configure:
    /*
    @Bean(name = "productMongoTemplate")
    public MongoTemplate productMongoTemplate(
            @Value("${mongodb.product.uri}") String productUri
    ) {
        MongoClient mongoClient = MongoClients.create(productUri);
        return new MongoTemplate(mongoClient, extractDatabaseName(productUri));
    }

    @Bean(name = "offerMongoTemplate")
    public MongoTemplate offerMongoTemplate(
            @Value("${mongodb.offer.uri}") String offerUri
    ) {
        MongoClient mongoClient = MongoClients.create(offerUri);
        return new MongoTemplate(mongoClient, extractDatabaseName(offerUri));
    }
    */

    private String extractDatabaseName(String uri) {
        // Extract DB name from URI: mongodb://host:port/dbname
        int lastSlash = uri.lastIndexOf('/');
        if (lastSlash > 0 && lastSlash < uri.length() - 1) {
            String dbPart = uri.substring(lastSlash + 1);
            // Remove query params if present
            int queryStart = dbPart.indexOf('?');
            return queryStart > 0 ? dbPart.substring(0, queryStart) : dbPart;
        }
        return "ecommerce_db"; // fallback
    }
}
