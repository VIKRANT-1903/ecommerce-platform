package com.example.ecomm.checkout.controller;

import com.example.ecomm.checkout.dto.CheckoutRequest;
import com.example.ecomm.checkout.dto.CheckoutResponse;
import com.example.ecomm.checkout.service.CheckoutService;
import com.example.ecomm.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/api/users/{userId}/checkout")
@RequiredArgsConstructor
public class CheckoutController {

    private final CheckoutService checkoutService;

    /**
     * Execute checkout for the user's cart. Orchestrates cart → order → reserve → payment → confirm/release → clear cart → notify.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<CheckoutResponse>> checkout(
            @PathVariable Integer userId,
            @Valid @RequestBody CheckoutRequest request) {
        CheckoutResponse response = checkoutService.checkout(userId, request);
        if (response.success()) {
            return ResponseEntity.ok(ApiResponse.success(response));
        }
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new ApiResponse<>(false, response, response.message(), "CHECKOUT_FAILED"));
    }
}
