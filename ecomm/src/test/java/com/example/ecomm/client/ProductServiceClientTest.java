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

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceClientTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private ProductServiceClient productServiceClient;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(productServiceClient, "restTemplate", restTemplate);
        ReflectionTestUtils.setField(productServiceClient, "ecomm1ServiceUrl", "http://mock-url");
    }

    @Test
    void testGetProductName_Success() {
        // Arrange
        String productId = "PROD-123";
        String expectedName = "Super Gadget";

        // Mock Response: {"data": {"name": "Super Gadget"}}
        Map<String, Object> productData = Map.of("name", expectedName);
        Map<String, Object> responseBody = Map.of("data", productData);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)
        )).thenReturn(ResponseEntity.ok(responseBody));

        // Act
        String result = productServiceClient.getProductName(productId);

        // Assert
        assertEquals(expectedName, result);
    }

    @Test
    void testGetProductName_NameMissing_ReturnsId() {
        // Arrange
        String productId = "PROD-123";

        // Mock Response: {"data": {}} (No name field)
        Map<String, Object> responseBody = Map.of("data", Map.of());

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)
        )).thenReturn(ResponseEntity.ok(responseBody));

        // Act
        String result = productServiceClient.getProductName(productId);

        // Assert
        assertEquals(productId, result); // Fallback to ID
    }

    @Test
    void testGetProductName_ServiceError_ReturnsId() {
        // Arrange
        String productId = "PROD-123";

        when(restTemplate.exchange(
                anyString(), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class)
        )).thenThrow(new RestClientException("Connection Timeout"));

        // Act
        String result = productServiceClient.getProductName(productId);

        // Assert
        assertEquals(productId, result); // Fallback to ID on error
    }
}