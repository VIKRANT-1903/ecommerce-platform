package com.example.searchservice.query;

public interface SuggestQueryBuilder {

    Object buildSuggestQuery(String prefix, int limit);
}
