package com.example.ecomm.cart.controller;

import com.example.ecomm.cart.dto.AddCartItemRequest;
import com.example.ecomm.cart.dto.CartResponse;
import com.example.ecomm.cart.dto.UpdateCartItemRequest;
import com.example.ecomm.cart.service.CartService;
import com.example.ecomm.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/{userId}/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    /**
     * Get or create the user's active cart. Returns the cart (creates an empty one if none exists).
     */
    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> getOrCreateCart(@PathVariable Integer userId) {
        CartResponse cart = cartService.getOrCreateCart(userId);
        return ResponseEntity.ok(ApiResponse.success(cart));
    }

    /**
     * Get the user's active cart. Returns 404 if no cart exists.
     */
    @GetMapping("/view")
    public ResponseEntity<ApiResponse<CartResponse>> getCart(@PathVariable Integer userId) {
        CartResponse cart = cartService.getCart(userId);
        return ResponseEntity.ok(ApiResponse.success(cart));
    }

    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CartResponse>> addItem(
            @PathVariable Integer userId,
            @Valid @RequestBody AddCartItemRequest request) {
        CartResponse cart = cartService.addItem(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(cart));
    }

    @PatchMapping("/items/{cartItemId}")
    public ResponseEntity<ApiResponse<CartResponse>> updateItemQuantity(
            @PathVariable Integer userId,
            @PathVariable Long cartItemId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        CartResponse cart = cartService.updateItemQuantity(userId, cartItemId, request);
        return ResponseEntity.ok(ApiResponse.success(cart));
    }

    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<ApiResponse<CartResponse>> removeItem(
            @PathVariable Integer userId,
            @PathVariable Long cartItemId) {
        CartResponse cart = cartService.removeItem(userId, cartItemId);
        return ResponseEntity.ok(ApiResponse.success(cart));
    }

    /**
     * List checked-out carts for a user (order history fallback).
     */
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<java.util.List<CartResponse>>> listCheckedOut(@PathVariable Integer userId) {
        java.util.List<CartResponse> history = cartService.listCheckedOutCarts(userId);
        return ResponseEntity.ok(ApiResponse.success(history));
    }

    /**
     * Fetch a specific checked-out cart by cartId for the user.
     */
    @GetMapping("/{cartId}")
    public ResponseEntity<ApiResponse<CartResponse>> getCheckedOutCart(
            @PathVariable Integer userId,
            @PathVariable Long cartId) {
        CartResponse cart = cartService.getCheckedOutCart(userId, cartId);
        return ResponseEntity.ok(ApiResponse.success(cart));
    }
}
