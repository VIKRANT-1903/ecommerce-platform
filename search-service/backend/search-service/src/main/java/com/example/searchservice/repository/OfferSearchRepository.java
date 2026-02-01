package com.example.searchservice.repository;

import com.example.searchservice.model.document.OfferDocument;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;

public interface OfferSearchRepository {

    SearchResponse<OfferDocument> search(Query query);
}
