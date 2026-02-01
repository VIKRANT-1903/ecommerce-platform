package com.example.searchservice.service;

import com.example.searchservice.controller.dto.request.FilterSearchRequestDTO;
import com.example.searchservice.controller.dto.request.ProductSearchRequestDTO;
import com.example.searchservice.controller.dto.response.ProductSearchResponseDTO;

public interface ProductSearchService {

    ProductSearchResponseDTO searchProducts(ProductSearchRequestDTO request);

    ProductSearchResponseDTO searchWithFilters(FilterSearchRequestDTO request);

    ProductSearchResponseDTO browseByCategory(String categoryId, int page, int size);
}
