package com.example.ecomm.inventory.dto;

import lombok.Builder;

@Builder
public record ReserveResult(
        boolean success,
        String message
) {
    public static ReserveResult ok() {
        return ReserveResult.builder().success(true).message("Reserved").build();
    }

    public static ReserveResult failure(String message) {
        return ReserveResult.builder().success(false).message(message).build();
    }
}
