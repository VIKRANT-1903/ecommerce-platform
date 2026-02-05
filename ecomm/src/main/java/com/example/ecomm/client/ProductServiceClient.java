package com.example.ecomm.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;

import java.util.Map;

/**
 * Client for fetching product information from ecomm1 service.
 */
@Component
@Slf4j
public class ProductServiceClient {

    private final RestTemplate restTemplate;

    @Value("${ecomm1.service.url:http://localhost:8082}")
    private String ecomm1ServiceUrl;

    public ProductServiceClient() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Fetch the product name for a given product ID.
     *
     * @param productId The product ID
     * @return The product name, or the productId if not found
     */
    public String getProductName(String productId) {
        try {
            String url = ecomm1ServiceUrl + "/products/" + productId;
            log.debug("Fetching product name from: {}", url);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            if (response.getBody() != null && response.getBody().get("data") != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> product = (Map<String, Object>) response.getBody().get("data");
                
                Object name = product.get("name");
                if (name != null) {
                    log.debug("Fetched product name for product={}: {}", productId, name);
                    return name.toString();
                }
            }

            log.warn("No product name found for product={}", productId);
            return productId; // Fallback to productId if not found
        } catch (Exception e) {
            log.error("Failed to fetch product name for product={}: {}", productId, e.getMessage());
            return productId; // Fallback to productId on error
        }
    }
}
