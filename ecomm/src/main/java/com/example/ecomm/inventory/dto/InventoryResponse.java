package com.example.ecomm.inventory.dto;

import lombok.Builder;

import java.time.Instant;

@Builder
public record InventoryResponse(
        Long inventoryId,
        String productId,
        Integer merchantId,
        Integer availableQty,
        Integer reservedQty,
        Instant updatedAt
) {}
