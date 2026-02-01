package com.example.searchservice.service.impl;

import com.example.searchservice.controller.dto.request.SuggestRequestDTO;
import com.example.searchservice.repository.SuggestRepository;
import com.example.searchservice.service.SuggestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SuggestServiceImpl implements SuggestService {

    //private final SuggestQueryBuilder queryBuilder;
    private final SuggestRepository repository;

    @Override
    public List<String> getSuggestions(SuggestRequestDTO request) {

        return repository.suggest(
                request.getPrefix(),
                request.getLimit()
        );
    }
}
