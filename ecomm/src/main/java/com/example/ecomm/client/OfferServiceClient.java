package com.example.ecomm.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Client for fetching offer prices from ecomm1 service.
 * This ensures we always use the real price from the database,
 * not trusting any client-sent prices.
 */
@Component
@Slf4j
public class OfferServiceClient {

    private final RestTemplate restTemplate;

    @Value("${ecomm1.service.url:http://localhost:8082}")
    private String ecomm1ServiceUrl;

    public OfferServiceClient() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Fetch the current price for a product from a specific merchant.
     *
     * @param productId The product ID
     * @param merchantId The merchant ID
     * @return The current price, or null if not found
     */
    public BigDecimal getOfferPrice(String productId, Integer merchantId) {
        try {
            String url = ecomm1ServiceUrl + "/offers/product/" + productId;
            log.debug("Fetching offer price from: {}", url);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            if (response.getBody() != null && response.getBody().get("data") != null) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> offers = (List<Map<String, Object>>) response.getBody().get("data");

                // Find the offer for this specific merchant
                for (Map<String, Object> offer : offers) {
                    Object offerMerchantId = offer.get("merchantId");
                    if (offerMerchantId != null && merchantId.equals(((Number) offerMerchantId).intValue())) {
                        Object price = offer.get("price");
                        if (price != null) {
                            BigDecimal fetchedPrice = new BigDecimal(price.toString());
                            log.info("Fetched real price for product={} merchant={}: {}", productId, merchantId, fetchedPrice);
                            return fetchedPrice;
                        }
                    }
                }

                // If no specific merchant offer found, use first available
                if (!offers.isEmpty()) {
                    Object price = offers.get(0).get("price");
                    if (price != null) {
                        BigDecimal fetchedPrice = new BigDecimal(price.toString());
                        log.warn("Merchant {} not found for product {}, using first offer price: {}", merchantId, productId, fetchedPrice);
                        return fetchedPrice;
                    }
                }
            }

            log.warn("No offer found for product={} merchant={}", productId, merchantId);
            return null;
        } catch (Exception e) {
            log.error("Failed to fetch offer price for product={} merchant={}: {}", productId, merchantId, e.getMessage());
            return null;
        }
    }
}
