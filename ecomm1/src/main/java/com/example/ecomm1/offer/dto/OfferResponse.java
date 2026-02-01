package com.example.ecomm1.offer.dto;

import com.example.ecomm1.offer.enums.OfferStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OfferResponse {
    private Long offerId;
    private String productId;
    private Long merchantId;
    private Double price;
    private String currency;
    private OfferStatus status;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}

