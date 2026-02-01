package com.example.searchservice.indexer.consumer;

import com.example.searchservice.indexer.event.CdcEvent;
import com.example.searchservice.indexer.model.OfferRow;
import com.example.searchservice.indexer.processor.OfferEventProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OfferCdcConsumer {

    private final OfferEventProcessor processor;

    @KafkaListener(
            topics = "offer.cdc",
            groupId = "search-indexer"
    )
    public void onMessage(CdcEvent<OfferRow> event) {
        processor.process(event);
    }
}
