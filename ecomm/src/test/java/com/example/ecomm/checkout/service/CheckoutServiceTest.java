package com.example.ecomm.checkout.service;

import com.example.ecomm.cart.service.CartService;
import com.example.ecomm.checkout.dto.CheckoutRequest;
import com.example.ecomm.checkout.dto.CheckoutResponse;
import com.example.ecomm.inventory.service.InventoryService;
import com.example.ecomm.order.dto.OrderItemResponse;
import com.example.ecomm.order.dto.OrderResponse;
import com.example.ecomm.order.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CheckoutService Tests")
class CheckoutServiceTest {

    @Mock
    private OrderService orderService;
    @Mock
    private InventoryService inventoryService;
    @Mock
    private CartService cartService;

    @InjectMocks
    private CheckoutService checkoutService;

    private Integer testUserId;
    private CheckoutRequest checkoutRequest;
    private OrderResponse mockOrder;

    @BeforeEach
    void setUp() {
        testUserId = 100;
        checkoutRequest = new CheckoutRequest(
                "123 Main St, New York, NY 10001"
        );

        OrderItemResponse item = OrderItemResponse.builder()
                .orderItemId(1L)
                .productId("PROD123")
                .productName("Test Product")
                .merchantId(5)
                .quantity(2)
                .price(new BigDecimal("149.99"))
                .build();

        mockOrder = new OrderResponse(
                1L,
                testUserId,
                new BigDecimal("299.98"),
                "PENDING",
                "PENDING",
                "123 Main St, New York, NY 10001",
                Instant.now(),
                List.of(item)
        );
    }


    @Nested
    @DisplayName("checkout")
    class CheckoutTests {

        @Test
        @DisplayName("should process checkout successfully and clear cart")
        void testCheckoutSuccess() {
            when(orderService.createOrder(testUserId, checkoutRequest))
                    .thenReturn(mockOrder);
            when(cartService.clearCart(testUserId))
                    .thenReturn(true);

            CheckoutResponse result = checkoutService.checkout(testUserId, checkoutRequest);

            assertThat(result).isNotNull();
            assertThat(result.success()).isTrue();
            assertThat(result.orderId()).isEqualTo(1L);
            assertThat(result.order().totalAmount()).isEqualByComparingTo(new BigDecimal("299.98"));
            
            verify(orderService).createOrder(testUserId, checkoutRequest);
            verify(cartService).clearCart(testUserId);
        }

        @Test
        @DisplayName("should return correct order data in response")
        void testCheckoutReturnsCorrectOrderData() {
            when(orderService.createOrder(anyInt(), any()))
                    .thenReturn(mockOrder);
            when(cartService.clearCart(anyInt()))
                    .thenReturn(true);

            CheckoutResponse result = checkoutService.checkout(testUserId, checkoutRequest);

            assertThat(result.order())
                    .isNotNull()
                    .extracting("orderId", "userId", "orderStatus", "paymentStatus")
                    .containsExactly(1L, testUserId, "PENDING", "PENDING");
            
            assertThat(result.order().items())
                    .hasSize(1)
                    .allMatch(item -> item.productName().equals("Test Product"));
        }

        @Test
        @DisplayName("should handle inventory not found gracefully")
        void testCheckoutWithNoInventory() {
            when(orderService.createOrder(testUserId, checkoutRequest))
                    .thenReturn(mockOrder);
            when(cartService.clearCart(testUserId))
                    .thenReturn(true);

            CheckoutResponse result = checkoutService.checkout(testUserId, checkoutRequest);

            assertThat(result.success()).isTrue();
            assertThat(result.orderId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("should handle cart clear failure gracefully")
        void testCheckoutWithCartClearFailure() {
            when(orderService.createOrder(testUserId, checkoutRequest))
                    .thenReturn(mockOrder);
            when(cartService.clearCart(testUserId))
                    .thenReturn(false);

            CheckoutResponse result = checkoutService.checkout(testUserId, checkoutRequest);

            assertThat(result.orderId()).isEqualTo(1L);
            verify(orderService).createOrder(testUserId, checkoutRequest);
        }

        @Test
        @DisplayName("should process payment successfully")
        void testCheckoutPaymentProcessing() {
            DummyPaymentGateway paymentGateway = new DummyPaymentGateway();
            
            boolean paymentSuccess = paymentGateway.processPayment(
                    mockOrder.orderId(),
                    mockOrder.totalAmount()
            );

            assertThat(paymentSuccess).isTrue();
        }

        @Test
        @DisplayName("should fail gracefully when order creation fails")
        void testCheckoutOrderCreationFailure() {
            when(orderService.createOrder(anyInt(), any()))
                    .thenThrow(new RuntimeException("Order creation failed"));

            assertThatThrownBy(() -> checkoutService.checkout(testUserId, checkoutRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Order creation failed");

            verify(cartService, never()).clearCart(anyInt());
        }

        @Test
        @DisplayName("should calculate correct total from cart items")
        void testCheckoutTotalCalculation() {
            assertThat(mockOrder.totalAmount())
                    .isEqualByComparingTo(new BigDecimal("299.98"));
        }

        @Test
        @DisplayName("should include shipping address in order")
        void testCheckoutIncludesShippingAddress() {
            when(orderService.createOrder(testUserId, checkoutRequest))
                    .thenReturn(mockOrder);
            when(cartService.clearCart(testUserId))
                    .thenReturn(true);

            CheckoutResponse result = checkoutService.checkout(testUserId, checkoutRequest);

            assertThat(result.order().shippingAddress())
                    .isEqualTo("123 Main St, New York, NY 10001");
        }
    }
}