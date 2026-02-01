package com.example.ecomm.checkout.dto;

import com.example.ecomm.order.dto.OrderResponse;
import lombok.Builder;

@Builder
public record CheckoutResponse(
        boolean success,
        Long orderId,
        String message,
        OrderResponse order
) {
    public static CheckoutResponse success(Long orderId, OrderResponse order) {
        return CheckoutResponse.builder()
                .success(true)
                .orderId(orderId)
                .message("Checkout completed successfully")
                .order(order)
                .build();
    }

    public static CheckoutResponse failure(Long orderId, String message) {
        return CheckoutResponse.builder()
                .success(false)
                .orderId(orderId)
                .message(message)
                .order(null)
                .build();
    }
}
