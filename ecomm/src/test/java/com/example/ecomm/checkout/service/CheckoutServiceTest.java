package com.example.ecomm.checkout.service;

import com.example.ecomm.cart.dto.CartItemResponse;
import com.example.ecomm.cart.dto.CartResponse;
import com.example.ecomm.cart.service.CartService;
import com.example.ecomm.checkout.dto.CheckoutRequest;
import com.example.ecomm.checkout.dto.CheckoutResponse;
import com.example.ecomm.common.exception.ResourceNotFoundException;
import com.example.ecomm.email.service.EmailService;
import com.example.ecomm.inventory.dto.ConfirmRequest;
import com.example.ecomm.inventory.dto.ReleaseRequest;
import com.example.ecomm.inventory.dto.ReserveRequest;
import com.example.ecomm.inventory.dto.ReserveResult;
import com.example.ecomm.inventory.service.InventoryService;
import com.example.ecomm.order.dto.CreateOrderRequest;
import com.example.ecomm.order.dto.OrderResponse;
import com.example.ecomm.order.entity.OrderStatus;
import com.example.ecomm.order.entity.PaymentStatus;
import com.example.ecomm.order.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CheckoutServiceTest {

    @Mock private CartService cartService;
    @Mock private OrderService orderService;
    @Mock private InventoryService inventoryService;
    @Mock private PaymentGateway paymentGateway;
    @Mock private EmailService emailService;

    @InjectMocks
    private CheckoutService checkoutService;

    private Integer userId = 101;
    private Long orderId = 5000L;
    private CheckoutRequest checkoutRequest;
    private CartResponse mockCart;
    private OrderResponse mockOrder;

    @BeforeEach
    void setUp() {
        checkoutRequest = CheckoutRequest.builder()
                .shippingAddress("123 Tech Street")
                .build();

        // Standard Cart with 1 item
        CartItemResponse item = CartItemResponse.builder()
                .productId("PROD-1")
                .merchantId(10)
                .quantity(2)
                .priceSnapshot(new BigDecimal("100.00"))
                .build();

        mockCart = CartResponse.builder()
                .cartId(1L)
                .userId(userId)
                .items(List.of(item))
                .build();

        mockOrder = OrderResponse.builder()
                .orderId(orderId)
                .userId(userId)
                .totalAmount(new BigDecimal("200.00"))
                .orderStatus(OrderStatus.CREATED.name())
                .build();
    }

    // --- SCENARIO 1: HAPPY PATH ---
    @Test
    void testCheckout_Success() {
        // 1. Mock Cart Fetch
        when(cartService.getCart(userId)).thenReturn(mockCart);

        // 2. Mock Order Creation
        when(orderService.createOrder(eq(userId), any(CreateOrderRequest.class))).thenReturn(mockOrder);

        // 3. Mock Inventory Reserve (Success)
        when(inventoryService.reserve(any(ReserveRequest.class)))
                .thenReturn(new ReserveResult(true, "Reserved"));

        // 4. Mock Payment (Success)
        when(paymentGateway.processPayment(orderId, mockOrder.totalAmount())).thenReturn(true);

        // 5. Mock Fetch Updated Order (for return)
        when(orderService.getOrder(orderId)).thenReturn(mockOrder);

        // Act
        CheckoutResponse response = checkoutService.checkout(userId, checkoutRequest);

        // Assert
        assertTrue(response.success());
        assertEquals(orderId, response.orderId());

        // Verify Flow
        verify(inventoryService, times(1)).reserve(any(ReserveRequest.class)); // Reserved
        verify(paymentGateway, times(1)).processPayment(eq(orderId), any()); // Paid
        verify(inventoryService, times(1)).confirm(any(ConfirmRequest.class)); // Confirmed
        verify(orderService).updateOrderStatus(orderId, OrderStatus.PAID, PaymentStatus.PAID); // Marked Paid
        verify(cartService).clearCart(userId); // Cart Cleared
        verify(emailService).sendOrderConfirmation(orderId, userId, true); // Email Sent
    }

    // --- SCENARIO 2: PAYMENT FAILURE (COMPENSATION) ---
    @Test
    void testCheckout_PaymentFailed_ShouldReleaseInventory() {
        // Arrange
        when(cartService.getCart(userId)).thenReturn(mockCart);
        when(orderService.createOrder(eq(userId), any())).thenReturn(mockOrder);
        when(inventoryService.reserve(any())).thenReturn(new ReserveResult(true, "Reserved"));

        // Mock Payment FAILS
        when(paymentGateway.processPayment(any(), any())).thenReturn(false);

        // Act
        CheckoutResponse response = checkoutService.checkout(userId, checkoutRequest);

        // Assert
        assertFalse(response.success());
        assertEquals("Payment failed", response.message());

        // Verify Compensation Logic
        verify(inventoryService, times(1)).reserve(any()); // Tried to reserve
        verify(inventoryService, times(1)).release(any(ReleaseRequest.class)); // COMPENSATION: Released!
        verify(orderService).updateOrderStatus(orderId, OrderStatus.FAILED, PaymentStatus.FAILED); // Marked Failed
        verify(cartService, never()).clearCart(anyInt()); // Cart should NOT be cleared so user can retry
    }

    // --- SCENARIO 3: INVENTORY RESERVATION FAILURE ---
    @Test
    void testCheckout_InventoryReserveFailed() {
        // Arrange
        when(cartService.getCart(userId)).thenReturn(mockCart);
        when(orderService.createOrder(eq(userId), any())).thenReturn(mockOrder);

        // Mock Inventory FAILS
        when(inventoryService.reserve(any()))
                .thenReturn(new ReserveResult(false, "Out of stock"));

        // Act
        CheckoutResponse response = checkoutService.checkout(userId, checkoutRequest);

        // Assert
        assertFalse(response.success());
        assertTrue(response.message().contains("Out of stock"));

        // Verify Flow
        verify(paymentGateway, never()).processPayment(any(), any()); // Should NOT charge user
        verify(inventoryService, never()).confirm(any()); // Should NOT confirm
        verify(orderService).updateOrderStatus(orderId, OrderStatus.FAILED, PaymentStatus.FAILED);
    }

    // --- SCENARIO 4: EMPTY CART ---
    @Test
    void testCheckout_EmptyCart_ThrowsException() {
        CartResponse emptyCart = CartResponse.builder().items(List.of()).build();
        when(cartService.getCart(userId)).thenReturn(emptyCart);

        assertThrows(IllegalArgumentException.class, () ->
                checkoutService.checkout(userId, checkoutRequest)
        );
    }
}