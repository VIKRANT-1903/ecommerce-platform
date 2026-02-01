package com.example.searchservice.query.impl;

import co.elastic.clients.json.JsonData;
import com.example.searchservice.controller.dto.request.FilterSearchRequestDTO;
import com.example.searchservice.controller.dto.request.ProductSearchRequestDTO;
import com.example.searchservice.query.ProductSearchQueryBuilder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MultiMatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Component
public class ProductSearchQueryBuilderImpl implements ProductSearchQueryBuilder {

    @Override
    public Query buildSearchQuery(ProductSearchRequestDTO request) {

        // If no query text, match all (used by landing pages)
        if (!StringUtils.hasText(request.getQuery())) {
            return Query.of(q -> q.matchAll(m -> m));
        }

        MultiMatchQuery multiMatchQuery = MultiMatchQuery.of(m -> m
                .query(request.getQuery())
                .fields(
                        "name^3",
                        "usp^2",
                        "description"
                )
        );

        return Query.of(q -> q.multiMatch(multiMatchQuery));
    }

    @Override
    public Query buildFilterQuery(FilterSearchRequestDTO request) {

        List<Query> mustQueries = new ArrayList<>();
        List<Query> filterQueries = new ArrayList<>();

        // -------- Full-text part --------
        if (StringUtils.hasText(request.getQuery())) {
            mustQueries.add(
                    Query.of(q -> q.multiMatch(m -> m
                            .query(request.getQuery())
                            .fields("name^3", "usp^2", "description")
                    ))
            );
        }

        // -------- Category filter --------
        if (StringUtils.hasText(request.getCategoryId())) {
            filterQueries.add(
                    Query.of(q -> q.term(t -> t
                            .field("category.id")
                            .value(request.getCategoryId())
                    ))
            );
        }

        // -------- Price range filter --------
        if (request.getMinPrice() != null || request.getMaxPrice() != null) {

            filterQueries.add(
                    Query.of(q -> q.range(
                            RangeQuery.of(rq -> rq.untyped(ut -> {
                                ut.field("minPrice");

                                if (request.getMinPrice() != null) {
                                    ut.gte(JsonData.of(request.getMinPrice()));
                                }

                                if (request.getMaxPrice() != null) {
                                    ut.lte(JsonData.of(request.getMaxPrice()));
                                }

                                return ut;
                            }))
                    ))
            );
        }

        // -------- In-stock filter --------
        if (Boolean.TRUE.equals(request.getInStockOnly())) {
            filterQueries.add(
                    Query.of(q -> q.term(t -> t
                            .field("inStock")
                            .value(true)
                    ))
            );
        }

        // -------- Rating filters --------
        if (request.getMinProductRating() != null) {

            filterQueries.add(
                    Query.of(q -> q.range(
                            RangeQuery.of(rq -> rq.untyped(ut -> ut
                                    .field("avgRating")
                                    .gte(JsonData.of(request.getMinProductRating()))
                            ))
                    ))
            );
        }


        BoolQuery boolQuery = BoolQuery.of(b -> {
            if (!mustQueries.isEmpty()) {
                b.must(mustQueries);
            }
            if (!filterQueries.isEmpty()) {
                b.filter(filterQueries);
            }
            return b;
        });

        return Query.of(q -> q.bool(boolQuery));
    }

    @Override
    public Query buildCategoryQuery(String categoryId) {

        return Query.of(q -> q.term(t -> t
                .field("category.id")
                .value(categoryId)
        ));
    }
}
