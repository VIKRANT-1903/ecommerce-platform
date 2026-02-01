package com.example.ecomm.cart.dto;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record CartItemResponse(
        Long cartItemId,
        String productId,
        Integer merchantId,
        Integer quantity,
        BigDecimal priceSnapshot
) {}
