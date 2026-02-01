package com.example.searchservice.indexer.consumer;
import com.example.searchservice.indexer.event.CdcEvent;
import com.example.searchservice.indexer.model.ProductRow;
import com.example.searchservice.indexer.processor.ProductEventProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductCdcConsumer {

    private final ProductEventProcessor processor;

    @KafkaListener(
            topics = "product.cdc",
            groupId = "search-indexer"
    )
    public void onMessage(CdcEvent<ProductRow> event) {
        processor.process(event);
    }
}
