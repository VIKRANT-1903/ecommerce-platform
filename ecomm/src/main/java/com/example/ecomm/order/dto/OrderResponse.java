package com.example.ecomm.order.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Builder
public record OrderResponse(
        Long orderId,            // Global ID (e.g., 505) - Internal use
        Integer userOrderNumber, // Friendly ID (e.g., 5) - Display use <--- NEW FIELD
        Integer userId,
        BigDecimal totalAmount,
        String orderStatus,
        String paymentStatus,
        String shippingAddress,
        Instant createdAt,
        List<OrderItemResponse> items
) {}