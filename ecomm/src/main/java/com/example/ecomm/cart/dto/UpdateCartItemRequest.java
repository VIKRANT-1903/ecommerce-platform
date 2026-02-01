package com.example.ecomm.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record UpdateCartItemRequest(
        @NotNull @Min(1) Integer quantity
) {}
