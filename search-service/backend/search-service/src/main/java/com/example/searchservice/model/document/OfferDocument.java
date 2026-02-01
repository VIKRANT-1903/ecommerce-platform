package com.example.searchservice.model.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OfferDocument {

    private String productId;
    private String merchantId;

    private double price;
    private String currency;

    private int availableQty;

    private double merchantRating;
    private double productRating;

    private long merchantSalesVolume;
    private int merchantCatalogSize;

    private String offerStatus;

    private Instant updatedAt;
}
