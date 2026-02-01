package com.example.ecomm.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record ConfirmRequest(
        @NotBlank String productId,
        @NotNull Integer merchantId,
        @NotNull @Min(1) Integer quantity
) {}
