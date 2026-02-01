package com.example.ecomm.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateInventoryRequest(
        @NotBlank(message = "Product ID is required")
        String productId,

        @NotNull(message = "Merchant ID is required")
        Integer merchantId,

        @NotNull(message = "Available quantity is required")
        @Min(value = 0, message = "Available quantity must be non-negative")
        Integer availableQty
) {}
