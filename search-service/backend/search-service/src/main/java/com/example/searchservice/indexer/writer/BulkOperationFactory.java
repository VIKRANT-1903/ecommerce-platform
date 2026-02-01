package com.example.searchservice.indexer.writer;

import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.json.JsonData;

public final class BulkOperationFactory {

    private BulkOperationFactory() {}

    public static BulkOperation upsert(
            String index,
            String id,
            Object document
    ) {
        return BulkOperation.of(b -> b
                .update(u -> u
                        .index(index)
                        .id(id)
                        .action(a -> a
                                .doc(JsonData.of(document))
                                .docAsUpsert(true)
                        )
                )
        );
    }

    public static BulkOperation delete(
            String index,
            String id
    ) {
        return BulkOperation.of(b -> b
                .delete(d -> d
                        .index(index)
                        .id(id)
                )
        );
    }
}
