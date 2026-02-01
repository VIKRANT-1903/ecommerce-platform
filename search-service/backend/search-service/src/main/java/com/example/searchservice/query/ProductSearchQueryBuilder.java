package com.example.searchservice.query;

import com.example.searchservice.controller.dto.request.FilterSearchRequestDTO;
import com.example.searchservice.controller.dto.request.ProductSearchRequestDTO;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;

public interface ProductSearchQueryBuilder {

    Query buildSearchQuery(ProductSearchRequestDTO request);

    Query buildFilterQuery(FilterSearchRequestDTO request);

    Query buildCategoryQuery(String categoryId);
}
