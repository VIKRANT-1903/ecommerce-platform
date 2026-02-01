package com.example.searchservice.repository;

import java.util.List;

public interface SuggestRepository {

    List<String> suggest(String prefix, int limit);
}
