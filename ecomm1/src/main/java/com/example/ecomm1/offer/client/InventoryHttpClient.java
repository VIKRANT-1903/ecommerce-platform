package com.example.ecomm1.offer.client;

import com.example.ecomm1.offer.dto.InventoryOfferRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.HttpClientErrorException;

@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryHttpClient {

    private final RestClient inventoryRestClient;

    /**
     * Validates that inventory exists for the given product/merchant by calling
     * GET /api/inventory?productId=...&merchantId=...
     *
     * If inventory doesn't exist, we log a warning but allow the offer to be created.
     * Inventory can be set up later by the merchant.
     */
    public void createOffer(InventoryOfferRequest request) {
        try {
            inventoryRestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/inventory")
                            .queryParam("productId", request.getProductId())
                            .queryParam("merchantId", request.getMerchantId())
                            .build())
                    .retrieve()
                    .toBodilessEntity();
            log.info("Inventory validated for productId={}, merchantId={}", 
                    request.getProductId(), request.getMerchantId());
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                // Inventory doesn't exist yet - this is OK, offer can still be created
                log.warn("No inventory found for productId={}, merchantId={}. Offer will be created without inventory validation.",
                        request.getProductId(), request.getMerchantId());
                return; // Don't throw, allow offer creation to proceed
            }
            // For other errors, log and continue (don't block offer creation)
            log.error("Inventory service error for productId={}, merchantId={}: {}",
                    request.getProductId(), request.getMerchantId(), e.getMessage());
        } catch (Exception e) {
            // Connection errors, timeouts, etc - log and continue
            log.error("Failed to reach inventory service for productId={}, merchantId={}: {}",
                    request.getProductId(), request.getMerchantId(), e.getMessage());
        }
    }

    public void deleteOffer(Long offerId) {
        // Current external contract does not define an offer-specific delete endpoint.
        // Inventory quantities are managed via reserve/confirm/release in a future order flow,
        // so we intentionally do not call the inventory service here.
    }
}

