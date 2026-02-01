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
public class ProductSearchResponseDTO {

    private List<ProductSummaryDTO> products;

    private long totalHits;
    private int page;
    private int size;

    private List<FacetDTO> facets;
}
