package com.example.searchservice.util;

public final class OfferRankingUtil {

    private OfferRankingUtil() {}

    /* -----------------------------
       Weights (sum = 1.0)
       ----------------------------- */

    public static final double PRICE_WEIGHT           = 0.30;
    public static final double MERCHANT_RATING_WEIGHT = 0.25;
    public static final double PRODUCT_RATING_WEIGHT  = 0.15;
    public static final double STOCK_WEIGHT           = 0.10;
    public static final double SALES_VOLUME_WEIGHT    = 0.10;
    public static final double CATALOG_SIZE_WEIGHT    = 0.10;

    /* -----------------------------
       Normalization caps
       ----------------------------- */

    public static final double MAX_RATING  = 5.0;
    public static final double MAX_SALES   = 100_000.0;
    public static final double MAX_CATALOG = 10_000.0;

    /* -----------------------------
       Painless scripts (SAFE)
       ----------------------------- */

    // Lower price → higher score
    public static String priceScoreScript() {
        return """
            if (doc['price'].size() == 0) return 0;
            return Math.min(1.0, params.minPrice / doc['price'].value);
        """;
    }

    public static String merchantRatingScript() {
        return """
            if (doc['merchantRating'].size() == 0) return 0;
            return doc['merchantRating'].value / params.maxRating;
        """;
    }

    public static String productRatingScript() {
        return """
            if (doc['productRating'].size() == 0) return 0;
            return doc['productRating'].value / params.maxRating;
        """;
    }

    public static String salesVolumeScript() {
        return """
            if (doc['merchantSalesVolume'].size() == 0) return 0;
            return Math.log(doc['merchantSalesVolume'].value + 1)
                 / Math.log(params.maxSales + 1);
        """;
    }

    public static String catalogSizeScript() {
        return """
            if (doc['merchantCatalogSize'].size() == 0) return 0;
            return Math.log(doc['merchantCatalogSize'].value + 1)
                 / Math.log(params.maxCatalog + 1);
        """;
    }
}
