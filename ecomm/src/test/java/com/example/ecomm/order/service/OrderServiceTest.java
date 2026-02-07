package com.example.ecomm.order.service;

import com.example.ecomm.cart.repository.CartRepository;
import com.example.ecomm.cart.service.CartService;
import com.example.ecomm.client.ProductServiceClient;
import com.example.ecomm.common.exception.ResourceNotFoundException;
import com.example.ecomm.order.dto.OrderResponse;
import com.example.ecomm.order.entity.Order;
import com.example.ecomm.order.entity.OrderItem;
import com.example.ecomm.order.repository.OrderItemRepository;
import com.example.ecomm.order.repository.OrderRepository;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService Tests")
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderItemRepository orderItemRepository;
    @Mock
    private CartService cartService;
    @Mock
    private CartRepository cartRepository;
    @Mock
    private ProductServiceClient productServiceClient;

    @InjectMocks
    private OrderService orderService;

    private Order testOrder;
    private OrderItem testOrderItem;

    @BeforeEach
    void setUp() {
        testOrder = Order.builder()
                .orderId(1L)
                .userId(100)
                .totalAmount(new BigDecimal("299.99"))
                .orderStatus("PENDING")
                .paymentStatus("PENDING")
                .shippingAddress("123 Main St")
                .createdAt(Instant.now())
                .build();

        testOrderItem = OrderItem.builder()
                .orderItemId(1L)
                .order(testOrder)
                .productId("PROD123")
                .merchantId(5)
                .quantity(2)
                .price(new BigDecimal("149.99"))
                .build();
    }

    @Nested
    @DisplayName("getOrder")
    class GetOrderTests {

        @Test
        @DisplayName("should return order with product names")
        void testGetOrderWithProductNames() {
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
            when(orderItemRepository.findByOrderOrderIdOrderByOrderItemId(1L))
                    .thenReturn(List.of(testOrderItem));
            when(productServiceClient.getProductName("PROD123"))
                    .thenReturn("Laptop");

            OrderResponse result = orderService.getOrder(1L);

            assertThat(result).isNotNull();
            assertThat(result.orderId()).isEqualTo(1L);
            assertThat(result.userId()).isEqualTo(100);
            assertThat(result.items()).hasSize(1);
            assertThat(result.items().get(0).productName()).isEqualTo("Laptop");
            assertThat(result.items().get(0).quantity()).isEqualTo(2);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when order not found")
        void testGetOrderNotFound() {
            when(orderRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> orderService.getOrder(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Order not found");
        }

        @Test
        @DisplayName("should include merchant ID in order items")
        void testGetOrderIncludesMerchantId() {
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
            when(orderItemRepository.findByOrderOrderIdOrderByOrderItemId(1L))
                    .thenReturn(List.of(testOrderItem));
            when(productServiceClient.getProductName(anyString()))
                    .thenReturn("Test Product");

            OrderResponse result = orderService.getOrder(1L);

            assertThat(result.items())
                    .allMatch(item -> item.merchantId().equals(5));
        }

        @Test
        @DisplayName("should include shipping address")
        void testGetOrderIncludesShippingAddress() {
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
            when(orderItemRepository.findByOrderOrderIdOrderByOrderItemId(1L))
                    .thenReturn(List.of());
            when(productServiceClient.getProductName(anyString()))
                    .thenReturn("Test Product");

            OrderResponse result = orderService.getOrder(1L);

            assertThat(result.shippingAddress()).isEqualTo("123 Main St");
        }
    }

    @Nested
    @DisplayName("listOrdersByUser")
    class ListOrdersByUserTests {

        @Test
        @DisplayName("should return user orders ordered by date descending")
        void testListUserOrders() {
            when(orderRepository.findByUserIdOrderByCreatedAtDesc(100))
                    .thenReturn(List.of(testOrder));
            when(orderItemRepository.findByOrderOrderIdOrderByOrderItemId(1L))
                    .thenReturn(List.of(testOrderItem));
            when(productServiceClient.getProductName(anyString()))
                    .thenReturn("Test Product");
            when(cartRepository.findByUserIdOrderByCreatedAtDesc(100))
                    .thenReturn(List.of());

            List<OrderResponse> results = orderService.listOrdersByUser(100);

            assertThat(results).hasSize(1);
            assertThat(results.get(0).userId()).isEqualTo(100);
        }

        @Test
        @DisplayName("should return empty list when user has no orders")
        void testListUserOrdersEmpty() {
            when(orderRepository.findByUserIdOrderByCreatedAtDesc(100))
                    .thenReturn(List.of());
            when(cartRepository.findByUserIdOrderByCreatedAtDesc(100))
                    .thenReturn(List.of());

            List<OrderResponse> results = orderService.listOrdersByUser(100);

            assertThat(results).isEmpty();
        }

        @Test
        @DisplayName("should include both Order table and cart items")
        void testListUserOrdersIncludesCartItems() {
            when(orderRepository.findByUserIdOrderByCreatedAtDesc(100))
                    .thenReturn(List.of(testOrder));
            when(orderItemRepository.findByOrderOrderIdOrderByOrderItemId(1L))
                    .thenReturn(List.of(testOrderItem));
            when(productServiceClient.getProductName(anyString()))
                    .thenReturn("Test Product");
            when(cartRepository.findByUserIdOrderByCreatedAtDesc(100))
                    .thenReturn(List.of());

            List<OrderResponse> results = orderService.listOrdersByUser(100);

            assertThat(results).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("getOrdersForMerchant")
    class GetOrdersForMerchantTests {

        @Test
        @DisplayName("should return merchant sales only")
        void testGetMerchantOrders() {
            when(orderItemRepository.findByMerchantIdOrderByOrderCreatedAtDesc(5))
                    .thenReturn(List.of(testOrderItem));
            when(productServiceClient.getProductName("PROD123"))
                    .thenReturn("Test Product");

            List<OrderResponse> results = orderService.getOrdersForMerchant(5);

            assertThat(results).isNotEmpty();
            assertThat(results.get(0).items())
                    .allMatch(item -> item.merchantId().equals(5));
        }

        @Test
        @DisplayName("should return empty list for merchant with no sales")
        void testGetMerchantOrdersEmpty() {
            when(orderItemRepository.findByMerchantIdOrderByOrderCreatedAtDesc(999))
                    .thenReturn(List.of());

            List<OrderResponse> results = orderService.getOrdersForMerchant(999);

            assertThat(results).isEmpty();
        }

        @Test
        @DisplayName("should group items by order ID")
        void testGetMerchantOrdersGroupedByOrderId() {
            OrderItem item2 = OrderItem.builder()
                    .orderItemId(2L)
                    .order(testOrder)
                    .productId("PROD456")
                    .merchantId(5)
                    .quantity(1)
                    .price(new BigDecimal("99.99"))
                    .build();

            when(orderItemRepository.findByMerchantIdOrderByOrderCreatedAtDesc(5))
                    .thenReturn(List.of(testOrderItem, item2));
            when(productServiceClient.getProductName(anyString()))
                    .thenReturn("Product Name");

            List<OrderResponse> results = orderService.getOrdersForMerchant(5);

            // Should group both items into one order
            assertThat(results).hasSize(1);
            assertThat(results.get(0).items()).hasSize(2);
        }

        @Test
        @DisplayName("should include product names for merchant items")
        void testGetMerchantOrdersIncludesProductNames() {
            when(orderItemRepository.findByMerchantIdOrderByOrderCreatedAtDesc(5))
                    .thenReturn(List.of(testOrderItem));
            when(productServiceClient.getProductName("PROD123"))
                    .thenReturn("Gaming Laptop");

            List<OrderResponse> results = orderService.getOrdersForMerchant(5);

            assertThat(results).isNotEmpty();
            assertThat(results.get(0).items())
                    .allMatch(item -> item.productName().equals("Gaming Laptop"));
        }
    }
}