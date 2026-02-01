package com.example.searchservice.service.impl;

import com.example.searchservice.controller.dto.request.FilterSearchRequestDTO;
import com.example.searchservice.controller.dto.request.ProductSearchRequestDTO;
import com.example.searchservice.controller.dto.response.ProductSearchResponseDTO;
import com.example.searchservice.controller.dto.response.ProductSummaryDTO;
import com.example.searchservice.model.document.ProductDocument;
import com.example.searchservice.query.ProductSearchQueryBuilder;
import com.example.searchservice.repository.ProductSearchRepository;
import com.example.searchservice.service.ProductSearchService;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductSearchServiceImpl implements ProductSearchService {

    private final ProductSearchQueryBuilder queryBuilder;
    private final ProductSearchRepository repository;

    @Override
    public ProductSearchResponseDTO searchProducts(ProductSearchRequestDTO request) {

        var query = queryBuilder.buildSearchQuery(request);
        SearchResponse<ProductDocument> response =
                repository.search(query, request.getPage(), request.getSize());

        return buildResponse(response, request.getPage(), request.getSize());
    }

    @Override
    public ProductSearchResponseDTO searchWithFilters(FilterSearchRequestDTO request) {

        var query = queryBuilder.buildFilterQuery(request);
        SearchResponse<ProductDocument> response =
                repository.search(query, request.getPage(), request.getSize());

        return buildResponse(response, request.getPage(), request.getSize());
    }

    @Override
    public ProductSearchResponseDTO browseByCategory(String categoryId, int page, int size) {

        var query = queryBuilder.buildCategoryQuery(categoryId);
        SearchResponse<ProductDocument> response =
                repository.search(query, page, size);

        return buildResponse(response, page, size);
    }

    // ----------------- Helpers -----------------

    private ProductSearchResponseDTO buildResponse(
            SearchResponse<ProductDocument> response,
            int page,
            int size
    ) {

        List<ProductSummaryDTO> products = response.hits()
                .hits()
                .stream()
                .map(hit -> mapToSummary(hit.source()))
                .collect(Collectors.toList());

        long totalHits = response.hits().total() != null
                ? response.hits().total().value()
                : products.size();

        return ProductSearchResponseDTO.builder()
                .products(products)
                .totalHits(totalHits)
                .page(page)
                .size(size)
                .facets(null) // Facets wired in next step
                .build();
    }

    private ProductSummaryDTO mapToSummary(ProductDocument doc) {

        if (doc == null) {
            return null;
        }

        return ProductSummaryDTO.builder()
                .productId(doc.getProductId())
                .name(doc.getName())
                .categoryName(
                        doc.getCategory() != null
                                ? doc.getCategory().getName()
                                : null
                )
                .minPrice(doc.getMinPrice())
                .merchantCount(doc.getMerchantCount())
                .inStock(doc.isInStock())
                .avgRating(doc.getAvgRating())
                .images(doc.getImages())
                .build();
    }
}
