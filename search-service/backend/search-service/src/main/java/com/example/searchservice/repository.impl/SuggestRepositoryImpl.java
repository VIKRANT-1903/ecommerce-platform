package com.example.searchservice.repository.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.CompletionSuggestOption;
import com.example.searchservice.config.SearchIndexProperties;
import com.example.searchservice.repository.SuggestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class SuggestRepositoryImpl implements SuggestRepository {

    private final ElasticsearchClient elasticsearchClient;
    private final SearchIndexProperties indexProperties;

    @Override
    public List<String> suggest(String prefix, int limit) {

        try {
            SearchResponse<Void> response = elasticsearchClient.search(
                    s -> s
                            .index(indexProperties.getSuggest())
                            .suggest(sg -> sg
                                    .suggesters("product-suggest", sug -> sug
                                            .prefix(prefix)
                                            .completion(c -> c
                                                    .field("suggest")
                                                    .size(limit)
                                            )
                                    )
                            ),
                    Void.class
            );

            return response.suggest()
                    .get("product-suggest")
                    .getFirst()
                    .completion()
                    .options()
                    .stream()
                    .map(CompletionSuggestOption::text)
                    .toList();

        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch suggestions", e);
        }
    }
}
