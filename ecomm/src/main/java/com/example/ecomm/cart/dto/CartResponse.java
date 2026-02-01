package com.example.ecomm.cart.dto;

import lombok.Builder;

import java.time.Instant;
import java.util.List;

@Builder
public record CartResponse(
        Long cartId,
        Integer userId,
        String status,
        Instant updatedAt,
        List<CartItemResponse> items
) {}
