package com.example.searchservice.indexer.controller;

import com.example.searchservice.indexer.service.SnapshotLoader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for triggering snapshot loading manually.
 * 
 * Security consideration:
 * - This endpoint should be protected (admin-only access)
 * - Consider requiring an API key or OAuth token
 */
@Slf4j
@RestController
@RequestMapping("/api/indexer")
@RequiredArgsConstructor
public class SnapshotLoaderController {

    private final SnapshotLoader snapshotLoader;

    /**
     * Trigger a full snapshot load.
     * 
     * POST /api/indexer/snapshot/load
     * 
     * This will:
     * - Load all active products
     * - Load all active offers
     * - Upsert them into Elasticsearch
     * 
     * Response: 202 Accepted (async processing)
     */
    @PostMapping("/snapshot/load")
    public ResponseEntity<String> loadSnapshot() {
        log.info("Snapshot load triggered via API");
        
        try {
            // Run synchronously for simplicity
            // TODO: Consider async execution with CompletableFuture or @Async
            snapshotLoader.loadSnapshot();
            
            return ResponseEntity.ok("Snapshot load completed successfully");
            
        } catch (Exception ex) {
            log.error("Snapshot load failed", ex);
            return ResponseEntity.status(500).body("Snapshot load failed: " + ex.getMessage());
        }
    }
}
