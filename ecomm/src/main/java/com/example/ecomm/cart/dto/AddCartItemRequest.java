package com.example.ecomm.cart.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record AddCartItemRequest(
        @NotBlank String productId,
        @NotNull Integer merchantId,
        @NotNull @Min(1) Integer quantity,
        @NotNull @DecimalMin("0.0") BigDecimal priceSnapshot
) {}
