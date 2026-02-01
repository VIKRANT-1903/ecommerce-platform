package com.example.searchservice.indexer.consumer;

import com.example.searchservice.indexer.event.CdcEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class InventoryCdcConsumer {

    @KafkaListener(
            topics = "inventory.cdc",
            groupId = "search-indexer"
    )
    public void onMessage(CdcEvent<?> event) {
        log.info("Inventory event received: {}", event);
    }
}
