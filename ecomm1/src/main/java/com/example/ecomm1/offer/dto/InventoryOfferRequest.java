package com.example.ecomm1.offer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryOfferRequest {
    private Long offerId;
    private String productId;
    private Long merchantId;
    private Double price;
    private String currency;
}

