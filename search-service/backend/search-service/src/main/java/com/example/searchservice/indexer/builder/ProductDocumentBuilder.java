package com.example.searchservice.indexer.builder;

import com.example.searchservice.indexer.model.OfferRow;
import com.example.searchservice.indexer.model.ProductRow;
import com.example.searchservice.model.document.ProductDocument;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ProductDocumentBuilder {

    /**
     * Builds a ProductDocument from ProductRow + aggregated offer data.
     * 
     * Aggregations:
     * - minPrice, maxPrice from active offers
     * - merchantCount (distinct merchants)
     * - inStock (any offer with availableQty > 0)
     */
    public ProductDocument build(ProductRow productRow, List<OfferRow> offers) {
        
        if (productRow == null) {
            log.warn("ProductRow is null, cannot build document");
            return null;
        }

        // Filter active offers
        List<OfferRow> activeOffers = offers.stream()
                .filter(offer -> "ACTIVE".equalsIgnoreCase(offer.getOfferStatus()))
                .filter(offer -> offer.getAvailableQty() > 0)
                .collect(Collectors.toList());

        // Compute aggregates
        double minPrice = activeOffers.stream()
                .mapToDouble(OfferRow::getPrice)
                .min()
                .orElse(0.0);

        double maxPrice = activeOffers.stream()
                .mapToDouble(OfferRow::getPrice)
                .max()
                .orElse(0.0);

        Set<String> uniqueMerchants = activeOffers.stream()
                .map(OfferRow::getMerchantId)
                .collect(Collectors.toSet());

        int merchantCount = uniqueMerchants.size();

        boolean inStock = !activeOffers.isEmpty();

        // Build category
        ProductDocument.Category category = new ProductDocument.Category(
                productRow.getCategoryId(),
                productRow.getCategoryName()
        );

        return ProductDocument.builder()
                .productId(productRow.getProductId())
                .name(productRow.getName())
                .description(productRow.getDescription())
                .category(category)
                .usp(productRow.getUsp())
                .attributes(productRow.getAttributes())
                .images(productRow.getImages())
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .merchantCount(merchantCount)
                .inStock(inStock)
                .avgRating(productRow.getAvgRating() != null ? productRow.getAvgRating() : 0.0)
                .popularityScore(productRow.getPopularityScore() != null ? productRow.getPopularityScore() : 0L)
                .createdAt(productRow.getCreatedAt())
                .build();
    }
}
