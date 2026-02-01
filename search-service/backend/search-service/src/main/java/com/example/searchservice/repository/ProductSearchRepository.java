package com.example.searchservice.repository;

import com.example.searchservice.model.document.ProductDocument;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;

public interface ProductSearchRepository {

    SearchResponse<ProductDocument> search(Query query, int page, int size);
}
