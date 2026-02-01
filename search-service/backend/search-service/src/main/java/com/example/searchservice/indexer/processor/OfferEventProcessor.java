package com.example.searchservice.indexer.processor;

import com.example.searchservice.config.SearchIndexProperties;
import com.example.searchservice.indexer.builder.OfferDocumentBuilder;
import com.example.searchservice.indexer.event.CdcEvent;
import com.example.searchservice.indexer.event.EventType;
import com.example.searchservice.indexer.model.OfferRow;
import com.example.searchservice.indexer.writer.ElasticBulkWriter;
import com.example.searchservice.model.document.OfferDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OfferEventProcessor {

    private final OfferDocumentBuilder documentBuilder;
    private final ElasticBulkWriter bulkWriter;
    private final SearchIndexProperties indexProperties;
    private final ProductEventProcessor productEventProcessor;

    /**
     * Process offer CDC events.
     * 
     * Flow:
     * 1. Upsert or delete offer in offer index
     * 2. Trigger product reindex for affected productId
     *    (to update aggregates: minPrice, maxPrice, merchantCount, inStock)
     */
    public void process(CdcEvent<OfferRow> event) {
        
        String offerId = event.getEntityId();
        EventType eventType = event.getEventType();
        
        log.info("Processing offer event: offerId={}, type={}", offerId, eventType);

        try {
            if (eventType == EventType.DELETE) {
                handleDelete(offerId, event.getPayload());
            } else {
                handleUpsert(event.getPayload());
            }
        } catch (Exception ex) {
            log.error("Failed to process offer event: {}", offerId, ex);
            // Next step: push to DLQ
            throw ex;
        }
    }

    private void handleUpsert(OfferRow offerRow) {
        
        if (offerRow == null) {
            log.warn("Offer payload is null, skipping");
            return;
        }

        String offerId = offerRow.getOfferId();
        String productId = offerRow.getProductId();

        // Skip inactive offers
        if ("INACTIVE".equalsIgnoreCase(offerRow.getOfferStatus())) {
            log.info("Offer {} is inactive, removing from index", offerId);
            handleDelete(offerId, offerRow);
            return;
        }

        // Build and upsert offer document
        OfferDocument document = documentBuilder.build(offerRow);
        
        if (document == null) {
            log.warn("Failed to build OfferDocument for {}", offerId);
            return;
        }

        bulkWriter.upsert(indexProperties.getOffer(), offerId, document);
        log.debug("Offer {} upserted to index", offerId);

        // Trigger product reindex to update aggregates
        triggerProductReindex(productId);
    }

    private void handleDelete(String offerId, OfferRow offerRow) {
        bulkWriter.delete(indexProperties.getOffer(), offerId);
        log.debug("Offer {} deleted from index", offerId);

        // Trigger product reindex to update aggregates
        if (offerRow != null && offerRow.getProductId() != null) {
            triggerProductReindex(offerRow.getProductId());
        }
    }

    /**
     * Trigger product reindex by creating a synthetic CDC event.
     * This ensures product aggregates (minPrice, maxPrice, etc.) stay in sync.
     * 
     * TODO: Consider debouncing/batching if multiple offers change simultaneously.
     */
    private void triggerProductReindex(String productId) {
        log.debug("Triggering product reindex for productId={}", productId);
        
        // TODO: Implement actual logic:
        // Option 1: Create synthetic ProductRow CDC event with UPDATE type
        // Option 2: Direct call to rebuild product document
        // Option 3: Queue product reindex job
        
        // Placeholder for now
        log.warn("Product reindex not yet implemented for productId={}", productId);
    }
}
