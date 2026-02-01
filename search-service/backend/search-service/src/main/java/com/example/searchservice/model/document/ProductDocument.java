package com.example.searchservice.model.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDocument {

    private String productId;

    private String name;
    private String description;

    private Category category;

    private List<String> usp;
    private Map<String, String> attributes;
    private List<String> images;

    // Aggregated / derived fields
    private double minPrice;
    private double maxPrice;
    private int merchantCount;
    private boolean inStock;
    private double avgRating;

    // Popularity & ranking signals
    private long popularityScore;

    private Instant createdAt;

    // ---------- Nested Types ----------

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Category {
        private String id;
        private String name;
    }
}
