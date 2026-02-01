package com.example.searchservice.controller.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacetDTO {

    private String name;   // brand, price_range, rating
    private String value;  // Apple, 20k-30k, 4+
    private long count;
}
