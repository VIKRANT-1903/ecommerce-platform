package com.example.ecomm.order.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Builder
public record OrderResponse(
        Long orderId,
        Integer userId,
        BigDecimal totalAmount,
        String orderStatus,
        String paymentStatus,
        String shippingAddress,
        Instant createdAt,
        List<OrderItemResponse> items
) {}
