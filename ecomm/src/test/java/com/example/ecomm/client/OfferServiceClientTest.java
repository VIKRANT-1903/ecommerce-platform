package com.example.ecomm.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OfferServiceClientTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private OfferServiceClient offerServiceClient;

    @BeforeEach
    void setUp() {
        // Since the class creates 'new RestTemplate()' in the constructor,
        // we must use Reflection to inject our Mock.
        ReflectionTestUtils.setField(offerServiceClient, "restTemplate", restTemplate);
        ReflectionTestUtils.setField(offerServiceClient, "ecomm1ServiceUrl", "http://mock-url");
    }

    @Test
    void testGetOfferPrice_SpecificMerchantFound() {
        // Arrange
        String productId = "PROD-1";
        Integer merchantId = 10;
        BigDecimal expectedPrice = new BigDecimal("99.99");

        // Mock Response: List containing our merchant
        List<Map<String, Object>> mockData = List.of(
                Map.of("merchantId", 5, "price", 120.00),
                Map.of("merchantId", 10, "price", 99.99)
        );
        Map<String, Object> responseBody = Map.of("data", mockData);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)
        )).thenReturn(ResponseEntity.ok(responseBody));

        // Act
        BigDecimal result = offerServiceClient.getOfferPrice(productId, merchantId);

        // Assert
        assertNotNull(result);
        assertEquals(expectedPrice, result);
    }

    @Test
    void testGetOfferPrice_MerchantNotFound_UseFallback() {
        // Arrange
        String productId = "PROD-1";
        Integer merchantId = 999; // Requesting non-existent merchant
        BigDecimal fallbackPrice = new BigDecimal("120.00");

        // Mock Response: List NOT containing 999, but has others
        List<Map<String, Object>> mockData = List.of(
                Map.of("merchantId", 5, "price", 120.00) // Should pick this one as fallback
        );
        Map<String, Object> responseBody = Map.of("data", mockData);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)
        )).thenReturn(ResponseEntity.ok(responseBody));

        // Act
        BigDecimal result = offerServiceClient.getOfferPrice(productId, merchantId);

        // Assert
        assertNotNull(result);
        assertEquals(fallbackPrice, result);
    }

    @Test
    void testGetOfferPrice_NoOffersAtAll() {
        // Arrange
        when(restTemplate.exchange(
                anyString(), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class)
        )).thenReturn(ResponseEntity.ok(Map.of("data", List.of()))); // Empty list

        // Act
        BigDecimal result = offerServiceClient.getOfferPrice("P1", 1);

        // Assert
        assertNull(result);
    }

    @Test
    void testGetOfferPrice_Exception_ReturnsNull() {
        // Arrange
        when(restTemplate.exchange(
                anyString(), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class)
        )).thenThrow(new RestClientException("Service Down"));

        // Act
        BigDecimal result = offerServiceClient.getOfferPrice("P1", 1);

        // Assert
        assertNull(result);
    }
}