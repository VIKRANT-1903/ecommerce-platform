package com.example.ecomm.cart.controller;

import com.example.ecomm.cart.dto.AddCartItemRequest;
import com.example.ecomm.cart.dto.CartResponse;
import com.example.ecomm.cart.dto.UpdateCartItemRequest;
import com.example.ecomm.cart.service.CartService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CartController.class)
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CartService cartService;

    @Autowired
    private ObjectMapper objectMapper;

    private CartResponse mockCartResponse;

    @BeforeEach
    void setUp() {
        mockCartResponse = CartResponse.builder()
                .cartId(500L)
                .userId(101)
                .status("ACTIVE")
                .items(List.of())
                .build();
    }

    // 1. Test GET Cart
    @Test
    void testGetCart_Success() throws Exception {
        when(cartService.getCart(101)).thenReturn(mockCartResponse);

        mockMvc.perform(get("/api/users/101/cart/view")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.cartId").value(500))
                .andExpect(jsonPath("$.data.userId").value(101));
    }

    // 2. Test ADD Item
    @Test
    void testAddItem_Success() throws Exception {
        AddCartItemRequest request = AddCartItemRequest.builder()
                .productId("P-001")
                .merchantId(10)
                .quantity(1)
                .priceSnapshot(BigDecimal.valueOf(100))
                .build();

        when(cartService.addItem(eq(101), any(AddCartItemRequest.class)))
                .thenReturn(mockCartResponse);

        mockMvc.perform(post("/api/users/101/cart/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated()) // Expects 201 Created
                .andExpect(jsonPath("$.data.cartId").value(500));
    }

    // 3. Test UPDATE Item Quantity
    @Test
    void testUpdateItemQuantity_Success() throws Exception {
        UpdateCartItemRequest request = UpdateCartItemRequest.builder()
                .quantity(5)
                .build();

        when(cartService.updateItemQuantity(eq(101), eq(99L), any(UpdateCartItemRequest.class)))
                .thenReturn(mockCartResponse);

        mockMvc.perform(patch("/api/users/101/cart/items/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // 4. Test Validation Failure (e.g., negative quantity)
    @Test
    void testAddItem_ValidationFail() throws Exception {
        AddCartItemRequest invalidRequest = AddCartItemRequest.builder()
                .productId("") // Blank ID (Invalid)
                .merchantId(10)
                .quantity(0)   // Min is 1 (Invalid)
                .build();

        mockMvc.perform(post("/api/users/101/cart/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest()); // Expects 400 Bad Request
    }
}