package com.example.searchservice.controller.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSummaryDTO {

    private String productId;
    private String name;
    private String categoryName;

    private double minPrice;
    private int merchantCount;
    private boolean inStock;
    private double avgRating;

    private List<String> images;
}
