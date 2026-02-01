package com.example.searchservice.service;

import com.example.searchservice.controller.dto.request.SuggestRequestDTO;

import java.util.List;

public interface SuggestService {

    List<String> getSuggestions(SuggestRequestDTO request);
}
