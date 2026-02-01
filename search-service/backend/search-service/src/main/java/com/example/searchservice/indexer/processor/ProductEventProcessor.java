package com.example.searchservice.indexer.processor;

import com.example.searchservice.config.SearchIndexProperties;
import com.example.searchservice.indexer.builder.ProductDocumentBuilder;
import com.example.searchservice.indexer.event.CdcEvent;
import com.example.searchservice.indexer.event.EventType;
import com.example.searchservice.indexer.model.OfferRow;
import com.example.searchservice.indexer.model.ProductRow;
import com.example.searchservice.indexer.repository.OfferDataRepository;
import com.example.searchservice.indexer.writer.ElasticBulkWriter;
import com.example.searchservice.model.document.ProductDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductEventProcessor {

    private final ProductDocumentBuilder documentBuilder;
    private final OfferDataRepository offerDataRepository;
    private final ElasticBulkWriter bulkWriter;
    private final SearchIndexProperties indexProperties;

    /**
     * Process product CDC events.
     * 
     * Flow:
     * 1. Extract ProductRow from event payload
     * 2. Fetch related offers for aggregation
     * 3. Build ProductDocument
     * 4. Upsert or delete in Elasticsearch
     */
    public void process(CdcEvent<ProductRow> event) {
        
        String productId = event.getEntityId();
        EventType eventType = event.getEventType();
        
        log.info("Processing product event: productId={}, type={}", productId, eventType);

        try {
            if (eventType == EventType.DELETE) {
                handleDelete(productId);
            } else {
                handleUpsert(event.getPayload());
            }
        } catch (Exception ex) {
            log.error("Failed to process product event: {}", productId, ex);
            // Next step: push to DLQ
            throw ex;
        }
    }

    private void handleUpsert(ProductRow productRow) {
        
        if (productRow == null) {
            log.warn("Product payload is null, skipping");
            return;
        }

        String productId = productRow.getProductId();

        // Skip inactive products
        if ("DELETED".equalsIgnoreCase(productRow.getStatus()) ||
            "INACTIVE".equalsIgnoreCase(productRow.getStatus())) {
            log.info("Product {} is inactive/deleted, removing from index", productId);
            handleDelete(productId);
            return;
        }

        // Fetch related offers for aggregation
        List<OfferRow> offers = offerDataRepository.findByProductId(productId);

        // Build document
        ProductDocument document = documentBuilder.build(productRow, offers);
        
        if (document == null) {
            log.warn("Failed to build ProductDocument for {}", productId);
            return;
        }

        // Upsert to Elasticsearch
        bulkWriter.upsert(indexProperties.getProduct(), productId, document);
        
        log.debug("Product {} upserted to index", productId);
    }

    private void handleDelete(String productId) {
        bulkWriter.delete(indexProperties.getProduct(), productId);
        log.debug("Product {} deleted from index", productId);
    }
}
