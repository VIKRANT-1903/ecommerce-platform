package com.example.searchservice.indexer.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "products")
public class ProductRow {

    @Id
    private String id; // MongoDB _id field
    
    private String name;
    private String description;
    
    private Category category; // Nested object
    
    private List<String> usp;
    private Map<String, String> attributes;
    private List<String> images;
    
    @Field("avg_rating")
    private Double avgRating; // Optional, may be null
    
    @Field("popularity_score")
    private Long popularityScore; // Optional, may be null
    
    @Field("created_at")
    private Instant createdAt;
    
    @Field("updated_at")
    private Instant updatedAt;
    
    private String status; // ACTIVE, INACTIVE, DELETED, OUT_OF_STOCK
    
    // Nested category class to match MongoDB structure
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Category {
        private String id; // Can be string like "c10" or numeric
        private String name;
    }
    
    // Helper methods
    public String getProductId() {
        return id; // Use MongoDB _id as productId
    }
    
    public String getCategoryId() {
        return category != null ? category.getId() : null;
    }
    
    public String getCategoryName() {
        return category != null ? category.getName() : null;
    }
}
