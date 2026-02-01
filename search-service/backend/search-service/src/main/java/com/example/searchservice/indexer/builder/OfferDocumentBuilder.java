package com.example.searchservice.indexer.builder;

import com.example.searchservice.indexer.model.OfferRow;
import com.example.searchservice.model.document.OfferDocument;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OfferDocumentBuilder {

    /**
     * Transforms OfferRow into OfferDocument for indexing.
     * 
     * Straightforward mapping with no aggregation.
     */
    public OfferDocument build(OfferRow offerRow) {
        
        if (offerRow == null) {
            log.warn("OfferRow is null, cannot build document");
            return null;
        }

        return OfferDocument.builder()
                .productId(offerRow.getProductId())
                .merchantId(offerRow.getMerchantId())
                .price(offerRow.getPrice())
                .currency(offerRow.getCurrency())
                .availableQty(offerRow.getAvailableQty())
                .merchantRating(offerRow.getMerchantRating())
                .productRating(offerRow.getProductRating())
                .merchantSalesVolume(offerRow.getMerchantSalesVolume())
                .merchantCatalogSize(offerRow.getMerchantCatalogSize())
                .offerStatus(offerRow.getOfferStatus())
                .updatedAt(offerRow.getUpdatedAt())
                .build();
    }
}
