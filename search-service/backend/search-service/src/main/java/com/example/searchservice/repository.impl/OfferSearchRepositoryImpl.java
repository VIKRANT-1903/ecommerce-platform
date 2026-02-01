package com.example.searchservice.repository.impl;

import com.example.searchservice.config.SearchIndexProperties;
import com.example.searchservice.model.document.OfferDocument;
import com.example.searchservice.repository.OfferSearchRepository;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OfferSearchRepositoryImpl implements OfferSearchRepository {

    private final ElasticsearchClient elasticsearchClient;
    private final SearchIndexProperties indexProperties;

    @Override
    public SearchResponse<OfferDocument> search(Query query) {
        try {
            return elasticsearchClient.search(
                    s -> s
                            .index(indexProperties.getOffer())
                            .query(query),
                    OfferDocument.class
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to execute offer search query", e);
        }
    }
}
