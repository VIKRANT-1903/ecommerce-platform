package com.example.searchservice.controller;

import com.example.searchservice.controller.dto.request.SuggestRequestDTO;
import com.example.searchservice.service.SuggestService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
public class SuggestController {

    private final SuggestService suggestService;

    /**
     * Autocomplete / typeahead
     * GET /search/suggest
     */
    @GetMapping("/suggest")
    public List<String> getSuggestions(SuggestRequestDTO request) {
        return suggestService.getSuggestions(request);
    }
}
