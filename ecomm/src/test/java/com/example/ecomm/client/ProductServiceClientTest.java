package com.example.ecomm.client;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductServiceClient Tests")
class ProductServiceClientTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private ProductServiceClient productServiceClient;

    @Nested
    @DisplayName("getProductName")
    class GetProductNameTests {

        @Test
        @DisplayName("should fetch product name successfully")
        void testGetProductNameSuccess() {
            Map<String, Object> productData = new HashMap<>();
            productData.put("name", "Gaming Laptop");
            productData.put("id", "PROD123");

            Map<String, Object> response = new HashMap<>();
            response.put("data", productData);

            when(restTemplate.exchange(
                    contains("/products/PROD123"),
                    eq(HttpMethod.GET),
                    isNull(),
                    any(ParameterizedTypeReference.class)
            )).thenReturn(ResponseEntity.ok(response));

            String result = productServiceClient.getProductName("PROD123");

            assertThat(result).isEqualTo("Gaming Laptop");
        }

        @Test
        @DisplayName("should return productId as fallback when name is null")
        void testGetProductNameFallbackWhenNull() {
            Map<String, Object> productData = new HashMap<>();
            productData.put("name", null);

            Map<String, Object> response = new HashMap<>();
            response.put("data", productData);

            when(restTemplate.exchange(
                    anyString(),
                    eq(HttpMethod.GET),
                    isNull(),
                    any(ParameterizedTypeReference.class)
            )).thenReturn(ResponseEntity.ok(response));

            String result = productServiceClient.getProductName("PROD123");

            assertThat(result).isEqualTo("PROD123");
        }

        @Test
        @DisplayName("should return productId as fallback when API returns null")
        void testGetProductNameFallbackWhenResponseNull() {
            when(restTemplate.exchange(
                    anyString(),
                    eq(HttpMethod.GET),
                    isNull(),
                    any(ParameterizedTypeReference.class)
            )).thenReturn(ResponseEntity.ok(null));

            String result = productServiceClient.getProductName("PROD456");

            assertThat(result).isEqualTo("PROD456");
        }

        @Test
        @DisplayName("should handle API exceptions gracefully")
        void testGetProductNameHandlesException() {
            when(restTemplate.exchange(
                    anyString(),
                    eq(HttpMethod.GET),
                    isNull(),
                    any(ParameterizedTypeReference.class)
            )).thenThrow(new RuntimeException("API Connection Error"));

            String result = productServiceClient.getProductName("PROD789");

            assertThat(result).isEqualTo("PROD789");
        }

        @Test
        @DisplayName("should handle empty response data")
        void testGetProductNameEmptyResponseData() {
            Map<String, Object> response = new HashMap<>();
            response.put("data", null);

            when(restTemplate.exchange(
                    anyString(),
                    eq(HttpMethod.GET),
                    isNull(),
                    any(ParameterizedTypeReference.class)
            )).thenReturn(ResponseEntity.ok(response));

            String result = productServiceClient.getProductName("PROD999");

            assertThat(result).isEqualTo("PROD999");
        }
    }
}