package com.example.ecomm.checkout.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record CheckoutRequest(
        @NotBlank String shippingAddress
) {}
