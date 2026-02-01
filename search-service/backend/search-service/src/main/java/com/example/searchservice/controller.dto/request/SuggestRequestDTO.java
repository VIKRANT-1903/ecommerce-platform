package com.example.searchservice.controller.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuggestRequestDTO {

    private String prefix;

    @Builder.Default
    private int limit = 10;
}
