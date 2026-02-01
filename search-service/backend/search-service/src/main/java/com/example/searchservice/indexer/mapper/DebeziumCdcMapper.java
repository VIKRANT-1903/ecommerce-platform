package com.example.searchservice.indexer.mapper;

import com.example.searchservice.indexer.event.CdcEvent;
import com.example.searchservice.indexer.event.DebeziumPayload;
import com.example.searchservice.indexer.event.EventType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.function.Function;

/**
 * Maps Debezium CDC payloads to CdcEvent.
 * 
 * Debezium ops:
 * - c = CREATE
 * - u = UPDATE
 * - d = DELETE
 * - r = READ (snapshot)
 * 
 * Tombstone handling:
 * - After a DELETE, Kafka sends a tombstone (null payload) for compaction
 * - We should handle this gracefully
 */
@Slf4j
@Component
public class DebeziumCdcMapper {

    /**
     * Map Debezium payload to CdcEvent.
     * 
     * @param debeziumPayload Raw Debezium event
     * @param entityIdExtractor Function to extract entity ID from the payload
     * @param <T> Type of the entity row
     * @return CdcEvent or null if tombstone
     */
    public <T> CdcEvent<T> map(
            DebeziumPayload<T> debeziumPayload,
            Function<T, String> entityIdExtractor
    ) {
        
        if (debeziumPayload == null) {
            log.warn("Received null Debezium payload (tombstone), skipping");
            return null;
        }

        String op = debeziumPayload.getOp();
        
        if (op == null) {
            log.warn("Debezium payload has null op field, skipping");
            return null;
        }

        EventType eventType = mapEventType(op);
        
        if (eventType == null) {
            log.warn("Unknown Debezium op: {}, skipping", op);
            return null;
        }

        T payload = extractPayload(debeziumPayload, eventType);
        
        if (payload == null) {
            log.warn("Payload is null for op={}, skipping", op);
            return null;
        }

        String entityId = entityIdExtractor.apply(payload);
        
        if (entityId == null) {
            log.error("Failed to extract entity ID from payload, skipping");
            return null;
        }

        CdcEvent<T> cdcEvent = new CdcEvent<>();
        cdcEvent.setEntityId(entityId);
        cdcEvent.setEventType(eventType);
        cdcEvent.setPayload(payload);
        cdcEvent.setVersion(debeziumPayload.getTs_ms());

        return cdcEvent;
    }

    private EventType mapEventType(String op) {
        switch (op.toLowerCase()) {
            case "c":
            case "r": // Snapshot reads are treated as CREATE
                return EventType.CREATE;
            case "u":
                return EventType.UPDATE;
            case "d":
                return EventType.DELETE;
            default:
                return null;
        }
    }

    /**
     * Extract the relevant payload based on operation type.
     * 
     * - CREATE/UPDATE: use 'after'
     * - DELETE: use 'before' (since 'after' is null)
     */
    private <T> T extractPayload(DebeziumPayload<T> debeziumPayload, EventType eventType) {
        if (eventType == EventType.DELETE) {
            return debeziumPayload.getBefore();
        } else {
            return debeziumPayload.getAfter();
        }
    }
}
