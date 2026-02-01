package com.example.searchservice.controller.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSearchRequestDTO {

    private String query;

    @Builder.Default
    private int page = 0;

    @Builder.Default
    private int size = 20;

    // Optional sorting (future-safe)
    private String sortBy;      // price | rating | popularity
    private String sortOrder;   // asc | desc
}
