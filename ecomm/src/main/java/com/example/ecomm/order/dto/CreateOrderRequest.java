package com.example.ecomm.order.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record CreateOrderRequest(
        @NotBlank String shippingAddress
) {}
