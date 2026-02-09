package com.example.ecomm.checkout.controller;

import com.example.ecomm.checkout.dto.CheckoutRequest;
import com.example.ecomm.checkout.dto.CheckoutResponse;
import com.example.ecomm.checkout.service.CheckoutService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean; // New in Spring Boot 3.4
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CheckoutController.class)
class CheckoutControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean // Replaces @MockBean in Spring Boot 3.4
    private CheckoutService checkoutService;

    @Test
    void testCheckout_Endpoint_Success() throws Exception {
        // Arrange
        Integer userId = 101;
        CheckoutRequest request = CheckoutRequest.builder()
                .shippingAddress("123 Main St")
                .build();

        CheckoutResponse successResponse = CheckoutResponse.builder()
                .success(true)
                .orderId(500L)
                .message("Success")
                .build();

        when(checkoutService.checkout(eq(userId), any(CheckoutRequest.class)))
                .thenReturn(successResponse);

        // Act & Assert
        mockMvc.perform(post("/api/users/{userId}/checkout", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.success").value(true))
                .andExpect(jsonPath("$.data.orderId").value(500));
    }

    @Test
    void testCheckout_Endpoint_Failure() throws Exception {
        // Arrange
        Integer userId = 101;
        CheckoutRequest request = CheckoutRequest.builder()
                .shippingAddress("123 Main St")
                .build();

        CheckoutResponse failureResponse = CheckoutResponse.builder()
                .success(false)
                .orderId(500L)
                .message("Payment Failed")
                .build();

        when(checkoutService.checkout(eq(userId), any(CheckoutRequest.class)))
                .thenReturn(failureResponse);

        // Act & Assert
        mockMvc.perform(post("/api/users/{userId}/checkout", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                // Controller logic returns 422 UNPROCESSABLE_ENTITY on failure
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.data.success").value(false))
                .andExpect(jsonPath("$.message").value("Payment Failed"));
    }
}
