package com.example.searchservice.controller.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FilterSearchRequestDTO {

    private String query;
    private String categoryId;

    private Double minPrice;
    private Double maxPrice;

    private Boolean inStockOnly;

    private Double minMerchantRating;
    private Double minProductRating;

    @Builder.Default
    private int page = 0;

    @Builder.Default
    private int size = 20;
}
