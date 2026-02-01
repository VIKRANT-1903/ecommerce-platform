package com.example.searchservice.indexer.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OfferRow {

    private String offerId;
    
    private String productId;
    private String merchantId;
    
    private double price;
    private String currency;
    
    private int availableQty;
    
    private double merchantRating;
    private double productRating;
    
    private long merchantSalesVolume;
    private int merchantCatalogSize;
    
    private String offerStatus; // ACTIVE, OUT_OF_STOCK, INACTIVE
    
    private Instant createdAt;
    private Instant updatedAt;
}
