package com.example.ecomm.order.dto;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record OrderItemResponse(
        Long orderItemId,
        String productId,
        String productName,
        Integer merchantId,
        Integer quantity,
        BigDecimal price
) {}
