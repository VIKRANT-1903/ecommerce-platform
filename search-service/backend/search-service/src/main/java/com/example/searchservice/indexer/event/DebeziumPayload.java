package com.example.searchservice.indexer.event;

import lombok.Data;

@Data
public class DebeziumPayload<T> {

    private T before;
    private T after;
    private String op; // c / u / d
    private long ts_ms;
}