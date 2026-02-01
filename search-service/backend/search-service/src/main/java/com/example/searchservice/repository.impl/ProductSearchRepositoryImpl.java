package com.example.searchservice.repository.impl;

import com.example.searchservice.config.SearchIndexProperties;
import com.example.searchservice.model.document.ProductDocument;
import com.example.searchservice.repository.ProductSearchRepository;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ProductSearchRepositoryImpl implements ProductSearchRepository {

    private final ElasticsearchClient elasticsearchClient;
    private final SearchIndexProperties indexProperties;

    @Override
    public SearchResponse<ProductDocument> search(Query query, int page, int size) {

        int from = page * size;

        try {
            return elasticsearchClient.search(s -> s
                            .index(indexProperties.getProduct())
                            .from(from)
                            .size(size)
                            .query(query),
                    ProductDocument.class
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to execute product search query", e);
        }
    }
}
