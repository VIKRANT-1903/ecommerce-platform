package com.example.searchservice.controller.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OfferResponseDTO {

    private String merchantId;
    private double price;
    private String currency;

    private int availableQty;

    private double merchantRating;
}
