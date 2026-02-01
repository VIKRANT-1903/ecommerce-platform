package com.example.searchservice.indexer.event;

import lombok.Data;

@Data
public class CdcEvent<T> {

    private String entityId;     // Kafka key
    private EventType eventType; // CREATE / UPDATE / DELETE
    private T payload;           // Row data
    private long version;        // LSN / timestamp / offset
}
