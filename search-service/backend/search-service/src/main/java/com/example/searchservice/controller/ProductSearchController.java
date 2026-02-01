package com.example.searchservice.controller;

import com.example.searchservice.controller.dto.request.FilterSearchRequestDTO;
import com.example.searchservice.controller.dto.request.ProductSearchRequestDTO;
import com.example.searchservice.controller.dto.response.ProductSearchResponseDTO;
import com.example.searchservice.service.ProductSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/search/products")
@RequiredArgsConstructor
public class ProductSearchController {

    private final ProductSearchService productSearchService;

    /**
     * Full-text product search
     * Example: GET /search/products?q=iphone&page=0&size=20
     */
    @GetMapping
    public ProductSearchResponseDTO searchProducts(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortOrder
    ) {

        ProductSearchRequestDTO request = ProductSearchRequestDTO.builder()
                .query(q)
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .sortOrder(sortOrder)
                .build();

        return productSearchService.searchProducts(request);
    }

    /**
     * Browse products by category
     * Example: GET curl "http://localhost:8083/search/products/by-category?categoryId=c10&page=0&size=20"
     */
    @GetMapping("/by-category")
    public ProductSearchResponseDTO browseByCategory(
            @RequestParam String categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return productSearchService.browseByCategory(categoryId, page, size);
    }

    /**
     * Search with filters
     * Example:
     * GET /search/products/filter?q=iphone&categoryId=c10&minPrice=20000&inStockOnly=true
     */
    @GetMapping("/filter")
    public ProductSearchResponseDTO searchWithFilters(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Boolean inStockOnly,
            @RequestParam(required = false) Double minMerchantRating,
            @RequestParam(required = false) Double minProductRating,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {

        FilterSearchRequestDTO request = FilterSearchRequestDTO.builder()
                .query(q)
                .categoryId(categoryId)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .inStockOnly(inStockOnly)
                .minMerchantRating(minMerchantRating)
                .minProductRating(minProductRating)
                .page(page)
                .size(size)
                .build();

        return productSearchService.searchWithFilters(request);
    }
}
