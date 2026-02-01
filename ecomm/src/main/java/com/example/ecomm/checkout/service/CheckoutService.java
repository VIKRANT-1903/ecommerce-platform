package com.example.ecomm.checkout.service;

import com.example.ecomm.cart.dto.CartItemResponse;
import com.example.ecomm.cart.dto.CartResponse;
import com.example.ecomm.cart.service.CartService;
import com.example.ecomm.checkout.dto.CheckoutRequest;
import com.example.ecomm.checkout.dto.CheckoutResponse;
import com.example.ecomm.common.exception.ResourceNotFoundException;
import com.example.ecomm.inventory.dto.ConfirmRequest;
import com.example.ecomm.inventory.dto.ReleaseRequest;
import com.example.ecomm.inventory.dto.ReserveRequest;
import com.example.ecomm.inventory.dto.ReserveResult;
import com.example.ecomm.inventory.service.InventoryService;
import com.example.ecomm.order.dto.CreateOrderRequest;
import com.example.ecomm.order.dto.OrderResponse;
import com.example.ecomm.order.entity.OrderStatus;
import com.example.ecomm.order.entity.PaymentStatus;
import com.example.ecomm.email.service.EmailService;
import com.example.ecomm.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CheckoutService {

    private final CartService cartService;
    private final OrderService orderService;
    private final InventoryService inventoryService;
    private final PaymentGateway paymentGateway;
    private final EmailService emailService;

    /**
     * Orchestrates the full checkout flow with compensation on failure.
     * Safe under concurrency (inventory service owns locking).
     */
    @Transactional
    public CheckoutResponse checkout(Integer userId, CheckoutRequest request) {
        // 1. Fetch cart
        CartResponse cart = cartService.getCart(userId);
        if (cart.items() == null || cart.items().isEmpty()) {
            throw new IllegalArgumentException("Cart is empty");
        }

        // 2. Create order
        CreateOrderRequest orderRequest = CreateOrderRequest.builder()
                .shippingAddress(request.shippingAddress())
                .build();
        OrderResponse order = orderService.createOrder(userId, orderRequest);
        Long orderId = order.orderId();

        // 3. Reserve inventory for each cart item
        List<ReserveRequest> reserved = new ArrayList<>();
        try {
            for (CartItemResponse item : cart.items()) {
                ReserveRequest reserveReq = ReserveRequest.builder()
                        .productId(item.productId())
                        .merchantId(item.merchantId())
                        .quantity(item.quantity())
                        .build();
                ReserveResult result = inventoryService.reserve(reserveReq);
                if (!result.success()) {
                    log.warn("Reserve failed for {}: {}", reserveReq, result.message());
                    compensateReserve(reserved);
                    completeFailure(orderId, userId);
                    cartService.clearCart(userId);
                    emailService.sendOrderConfirmation(orderId, userId, false);
                    return CheckoutResponse.failure(orderId, result.message());
                }
                reserved.add(reserveReq);
            }
        } catch (ResourceNotFoundException e) {
            log.warn("Reserve failed: {}", e.getMessage());
            compensateReserve(reserved);
            completeFailure(orderId, userId);
            cartService.clearCart(userId);
            emailService.sendOrderConfirmation(orderId, userId, false);
            return CheckoutResponse.failure(orderId, e.getMessage());
        }

        // 4. Call payment gateway
        boolean paymentSuccess = paymentGateway.processPayment(orderId, order.totalAmount());

        if (!paymentSuccess) {
            // 5b. Payment failure: release inventory, mark FAILED, clear cart, notify
            log.info("Payment failed for order {}, releasing inventory", orderId);
            compensateReserve(reserved);
            completeFailure(orderId, userId);
            cartService.clearCart(userId);
            emailService.sendOrderConfirmation(orderId, userId, false);
            return CheckoutResponse.failure(orderId, "Payment failed");
        }

        // 5a. Payment success: confirm inventory, mark PAID, clear cart, notify
        try {
            for (CartItemResponse item : cart.items()) {
                ConfirmRequest confirmReq = ConfirmRequest.builder()
                        .productId(item.productId())
                        .merchantId(item.merchantId())
                        .quantity(item.quantity())
                        .build();
                inventoryService.confirm(confirmReq);
            }
        } catch (Exception e) {
            log.error("Confirm failed for order {}: {}", orderId, e.getMessage());
            compensateReserve(reserved);
            completeFailure(orderId, userId);
            cartService.clearCart(userId);
            emailService.sendOrderConfirmation(orderId, userId, false);
            return CheckoutResponse.failure(orderId, "Inventory confirm failed: " + e.getMessage());
        }

        orderService.updateOrderStatus(orderId, OrderStatus.PAID, PaymentStatus.PAID);
        cartService.clearCart(userId);
        emailService.sendOrderConfirmation(orderId, userId, true);

        OrderResponse updatedOrder = orderService.getOrder(orderId);
        log.info("Checkout completed for order {} user {}", orderId, userId);
        return CheckoutResponse.success(orderId, updatedOrder);
    }

    private void compensateReserve(List<ReserveRequest> reserved) {
        for (ReserveRequest req : reserved) {
            try {
                ReleaseRequest releaseReq = ReleaseRequest.builder()
                        .productId(req.productId())
                        .merchantId(req.merchantId())
                        .quantity(req.quantity())
                        .build();
                inventoryService.release(releaseReq);
                log.debug("Released {} for product {} merchant {}", req.quantity(), req.productId(), req.merchantId());
            } catch (Exception e) {
                log.error("Failed to release {}: {}", req, e.getMessage());
            }
        }
    }

    private void completeFailure(Long orderId, Integer userId) {
        try {
            orderService.updateOrderStatus(orderId, OrderStatus.FAILED, PaymentStatus.FAILED);
        } catch (Exception e) {
            log.error("Failed to mark order {} as FAILED: {}", orderId, e.getMessage());
        }
    }
}
