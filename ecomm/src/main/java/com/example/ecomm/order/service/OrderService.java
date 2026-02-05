package com.example.ecomm.order.service;

import com.example.ecomm.cart.dto.CartItemResponse;
import com.example.ecomm.cart.dto.CartResponse;
import com.example.ecomm.cart.service.CartService;
import com.example.ecomm.common.exception.ResourceNotFoundException;
import com.example.ecomm.order.dto.CreateOrderRequest;
import com.example.ecomm.order.dto.OrderItemResponse;
import com.example.ecomm.order.dto.OrderResponse;
import com.example.ecomm.order.entity.Order;
import com.example.ecomm.order.entity.OrderItem;
import com.example.ecomm.order.entity.OrderStatus;
import com.example.ecomm.order.entity.PaymentStatus;
import com.example.ecomm.order.repository.OrderItemRepository;
import com.example.ecomm.order.repository.OrderRepository;
import com.example.ecomm.cart.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final CartService cartService;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartRepository cartRepository;

    /**
     * Create an order from the user's current cart. Total amount is calculated from cart item prices.
     * Order items are immutable once created (price snapshot from cart).
     */
    @Transactional
    public OrderResponse createOrder(Integer userId, CreateOrderRequest request) {
        CartResponse cart = cartService.getCart(userId);
        if (cart.items() == null || cart.items().isEmpty()) {
            throw new IllegalArgumentException("Cart is empty");
        }

        BigDecimal totalAmount = cart.items().stream()
                .map(item -> item.priceSnapshot().multiply(BigDecimal.valueOf(item.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = Order.builder()
                .userId(userId)
                .totalAmount(totalAmount)
                .orderStatus(OrderStatus.CREATED)
                .paymentStatus(PaymentStatus.PENDING)
                .shippingAddress(request.shippingAddress())
                .build();
        order = orderRepository.save(order);

        for (CartItemResponse cartItem : cart.items()) {
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .productId(cartItem.productId())
                    .merchantId(cartItem.merchantId())
                    .quantity(cartItem.quantity())
                    .price(cartItem.priceSnapshot())
                    .build();
            orderItemRepository.save(orderItem);
        }

        log.info("Created order {} for user {}", order.getOrderId(), userId);
        return toOrderResponse(order);
    }

    @Transactional
    public void updateOrderStatus(Long orderId, OrderStatus orderStatus, PaymentStatus paymentStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
        order.setOrderStatus(orderStatus);
        order.setPaymentStatus(paymentStatus);
        orderRepository.save(order);
        log.info("Updated order {} status to {} / {}", orderId, orderStatus, paymentStatus);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .map(this::toOrderResponse)
                .orElseGet(() -> {
                    // Fallback: check if this id refers to a checked-out cart
                    com.example.ecomm.cart.entity.Cart cart = cartRepository.findById(orderId)
                        .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
                    if (cart.getStatus() != com.example.ecomm.cart.entity.CartStatus.CHECKED_OUT) {
                    throw new ResourceNotFoundException("Order not found: " + orderId);
                    }
                    // Map cart entity -> OrderResponse
                    java.math.BigDecimal total = cart.getItems().stream()
                        .map(i -> i.getPriceSnapshot().multiply(java.math.BigDecimal.valueOf(i.getQuantity())))
                        .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

                    List<OrderItemResponse> items = cart.getItems().stream()
                        .map(i -> OrderItemResponse.builder()
                            .orderItemId(i.getCartItemId())
                            .productId(i.getProductId())
                            .merchantId(i.getMerchantId())
                            .quantity(i.getQuantity())
                            .price(i.getPriceSnapshot())
                            .build())
                        .toList();

                    return OrderResponse.builder()
                        .orderId(cart.getCartId())
                        .userId(cart.getUserId())
                        .totalAmount(total)
                        .orderStatus(cart.getStatus().name())
                        .paymentStatus("PAID")
                        .shippingAddress(null)
                        .createdAt(cart.getUpdatedAt())
                        .items(items)
                        .build();
                });
    }

        @Transactional(readOnly = true)
        public List<OrderResponse> listOrdersByUser(Integer userId) {
        // Query Order table (new checkout flow)
        List<OrderResponse> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId)
            .stream()
            .map(this::toOrderResponse)
            .toList();
        
        // Also include checked-out carts (legacy flow)
        List<CartResponse> carts = cartService.listCheckedOutCarts(userId);
        List<OrderResponse> cartOrders = carts.stream()
            .map(this::toOrderResponseFromCart)
            .toList();
        
        // Combine and sort by creation date descending
        List<OrderResponse> allOrders = new java.util.ArrayList<>(orders);
        allOrders.addAll(cartOrders);
        allOrders.sort((a, b) -> b.createdAt().compareTo(a.createdAt()));
        
        return allOrders;
        }

        private OrderResponse toOrderResponseFromCart(CartResponse cart) {
        java.math.BigDecimal total = cart.items().stream()
            .map(i -> i.priceSnapshot().multiply(java.math.BigDecimal.valueOf(i.quantity())))
            .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        List<OrderItemResponse> items = cart.items().stream()
            .map(i -> OrderItemResponse.builder()
                .orderItemId(i.cartItemId())
                .productId(i.productId())
                .merchantId(i.merchantId())
                .quantity(i.quantity())
                .price(i.priceSnapshot())
                .build())
            .toList();

        return OrderResponse.builder()
            .orderId(cart.cartId())
            .userId(cart.userId())
            .totalAmount(total)
            .orderStatus(cart.status())
            .paymentStatus("PAID")
            .shippingAddress(null)
            .createdAt(cart.updatedAt())
            .items(items)
            .build();
        }

    private OrderResponse toOrderResponse(Order order) {
        List<OrderItemResponse> items = orderItemRepository.findByOrderOrderIdOrderByOrderItemId(order.getOrderId())
                .stream()
                .map(this::toOrderItemResponse)
                .toList();
        return OrderResponse.builder()
                .orderId(order.getOrderId())
                .userId(order.getUserId())
                .totalAmount(order.getTotalAmount())
                .orderStatus(order.getOrderStatus().name())
                .paymentStatus(order.getPaymentStatus().name())
                .shippingAddress(order.getShippingAddress())
                .createdAt(order.getCreatedAt())
                .items(items)
                .build();
    }

    /**
     * Get orders (sales) for a specific merchant based on items they sold.
     * Groups items by order to avoid duplicates.
     */
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersForMerchant(Integer merchantId) {
        // Get all order items sold by this merchant
        List<OrderItem> merchantItems = orderItemRepository.findByMerchantIdOrderByOrderCreatedAtDesc(merchantId);
        
        // Group by orderId to get unique orders
        java.util.Map<Long, Order> ordersMap = new java.util.LinkedHashMap<>();
        for (OrderItem item : merchantItems) {
            ordersMap.putIfAbsent(item.getOrder().getOrderId(), item.getOrder());
        }
        
        // Convert to OrderResponse
        return ordersMap.values().stream()
            .map(this::toOrderResponse)
            .toList();
    }

    private OrderItemResponse toOrderItemResponse(OrderItem item) {
        return OrderItemResponse.builder()
                .orderItemId(item.getOrderItemId())
                .productId(item.getProductId())
                .merchantId(item.getMerchantId())
                .quantity(item.getQuantity())
                .price(item.getPrice())
                .build();
    }
}
