package com.example.searchservice.indexer.writer;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ElasticBulkWriter {

    private static final int MAX_BULK_SIZE = 200;
    private static final int MAX_RETRIES = 3;

    private final ElasticsearchClient elasticsearchClient;

    private final List<BulkOperation> buffer = new ArrayList<>();

    // ---------- Public API ----------

    public synchronized void upsert(String index, String id, Object document) {
        buffer.add(
                BulkOperationFactory.upsert(index, id, document)
        );
        flushIfNeeded();
    }

    public synchronized void delete(String index, String id) {
        buffer.add(
                BulkOperationFactory.delete(index, id)
        );
        flushIfNeeded();
    }

    public synchronized void flush() {
        if (buffer.isEmpty()) {
            return;
        }

        List<BulkOperation> batch = new ArrayList<>(buffer);
        buffer.clear();

        executeWithRetry(batch);
    }

    // ---------- Internal helpers ----------

    private void flushIfNeeded() {
        if (buffer.size() >= MAX_BULK_SIZE) {
            flush();
        }
    }

    private void executeWithRetry(List<BulkOperation> operations) {

        int attempt = 0;

        while (attempt < MAX_RETRIES) {
            attempt++;

            try {
                BulkRequest request = BulkRequest.of(b -> b
                        .operations(operations)
                );

                BulkResponse response =
                        elasticsearchClient.bulk(request);

                if (!response.errors()) {
                    log.debug("Bulk indexed {} operations", operations.size());
                    return;
                }

                // Partial failures
                log.warn("Bulk had errors, attempt {}", attempt);
                handlePartialFailures(response, operations);

            } catch (Exception ex) {
                log.error(
                        "Bulk indexing failed on attempt {}",
                        attempt,
                        ex
                );
            }

            sleepBackoff(attempt);
        }

        // After retries exhausted
        log.error(
                "Bulk indexing permanently failed for {} operations",
                operations.size()
        );

        // Next step: push to DLQ (we’ll wire this later)
    }

    private void handlePartialFailures(
            BulkResponse response,
            List<BulkOperation> operations
    ) {

        response.items().forEach(item -> {
            if (item.error() != null) {
                log.error(
                        "Failed bulk item: index={}, id={}, reason={}",
                        item.index(),
                        item.id(),
                        item.error().reason()
                );
                // Later: send this op to DLQ
            }
        });
    }

    private void sleepBackoff(int attempt) {
        try {
            Thread.sleep(100L * attempt);
        } catch (InterruptedException ignored) {
        }
    }
}
