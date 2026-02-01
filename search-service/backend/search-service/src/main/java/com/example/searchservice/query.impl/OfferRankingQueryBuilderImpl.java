package com.example.searchservice.query.impl;

import com.example.searchservice.query.OfferRankingQueryBuilder;
import com.example.searchservice.util.OfferRankingUtil;
import co.elastic.clients.elasticsearch._types.query_dsl.FunctionBoostMode;
import co.elastic.clients.elasticsearch._types.query_dsl.FunctionScoreMode;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.json.JsonData;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class OfferRankingQueryBuilderImpl implements OfferRankingQueryBuilder {

    @Override
    public Query buildOfferRankingQuery(String productId) {

        return Query.of(q -> q
                .functionScore(fs -> fs
                        .query(qb -> qb
                                .term(t -> t
                                        .field("productId")
                                        .value(productId)
                                )
                        )
                        .scoreMode(FunctionScoreMode.Sum)
                        .boostMode(FunctionBoostMode.Replace)

                        // 1️⃣ Price (lower is better)
                        .functions(f -> f
                                .scriptScore(ss -> ss
                                        .script(s -> s
                                                .source(OfferRankingUtil.priceScoreScript())
                                                .params(Map.of(
                                                        "minPrice", JsonData.of(1.0)
                                                ))
                                        )
                                )
                                .weight(OfferRankingUtil.PRICE_WEIGHT)
                        )

                        // 2️⃣ Merchant rating
                        .functions(f -> f
                                .scriptScore(ss -> ss
                                        .script(s -> s
                                                .source(OfferRankingUtil.merchantRatingScript())
                                                .params(Map.of(
                                                        "maxRating", JsonData.of(OfferRankingUtil.MAX_RATING)
                                                ))
                                        )
                                )
                                .weight(OfferRankingUtil.MERCHANT_RATING_WEIGHT)
                        )

                        // 3️⃣ Product rating
                        .functions(f -> f
                                .scriptScore(ss -> ss
                                        .script(s -> s
                                                .source(OfferRankingUtil.productRatingScript())
                                                .params(Map.of(
                                                        "maxRating", JsonData.of(OfferRankingUtil.MAX_RATING)
                                                ))
                                        )
                                )
                                .weight(OfferRankingUtil.PRODUCT_RATING_WEIGHT)
                        )

                        // 4️⃣ Stock availability (demotion)
                        .functions(f -> f
                                .filter(qf -> qf
                                        .range(r -> r
                                                .untyped(ut -> ut
                                                        .field("availableQty")
                                                        .gt(JsonData.of(0))
                                                )
                                        )
                                )

                                .weight(OfferRankingUtil.STOCK_WEIGHT)
                        )

                        // 5️⃣ Merchant sales volume
                        .functions(f -> f
                                .scriptScore(ss -> ss
                                        .script(s -> s
                                                .source(OfferRankingUtil.salesVolumeScript())
                                                .params(Map.of(
                                                        "maxSales", JsonData.of(OfferRankingUtil.MAX_SALES)
                                                ))
                                        )
                                )
                                .weight(OfferRankingUtil.SALES_VOLUME_WEIGHT)
                        )

                        // 6️⃣ Merchant catalog size
                        .functions(f -> f
                                .scriptScore(ss -> ss
                                        .script(s -> s
                                                .source(OfferRankingUtil.catalogSizeScript())
                                                .params(Map.of(
                                                        "maxCatalog", JsonData.of(OfferRankingUtil.MAX_CATALOG)
                                                ))
                                        )
                                )
                                .weight(OfferRankingUtil.CATALOG_SIZE_WEIGHT)
                        )
                )
        );
    }
}
