package com.example.searchservice.query;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;

public interface OfferRankingQueryBuilder {

    Query buildOfferRankingQuery(String productId);
}
