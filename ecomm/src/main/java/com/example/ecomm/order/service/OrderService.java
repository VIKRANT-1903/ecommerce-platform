package com.example.ecomm.order.service;

import com.example.ecomm.cart.dto.CartItemResponse;
import com.example.ecomm.cart.dto.CartResponse;
import com.example.ecomm.cart.service.CartService;
import com.example.ecomm.client.ProductServiceClient;
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
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final CartService cartService;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartRepository cartRepository;
    private final ProductServiceClient productServiceClient;

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

        // 1. SAVE: Generates the global ID (e.g., 57)
        order = orderRepository.save(order);

        // 2. CALCULATE FRIENDLY NUMBER (e.g., 1)
        long friendlyCount = orderRepository.countByUserIdAndOrderIdLessThanEqual(userId, order.getOrderId());

        // 3. ATTACH TO OBJECT: Now the Email Service can read 'order.getUserOrderNumber()'
        order.setUserOrderNumber((int) friendlyCount);

        List<OrderItem> savedItems = new ArrayList<>();
        for (CartItemResponse cartItem : cart.items()) {
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .productId(cartItem.productId())
                    .merchantId(cartItem.merchantId())
                    .quantity(cartItem.quantity())
                    .price(cartItem.priceSnapshot())
                    .build();
            orderItemRepository.save(orderItem);
            savedItems.add(orderItem);
        }
        order.setItems(savedItems);

        log.info("Created order #{} (Global ID: {}) for user {}", friendlyCount, order.getOrderId(), userId);

        // The 'order' object now has the correct number inside it.
        // When your EmailListener / EmailService picks this up, it will see the correct number.

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
                    // Fallback for legacy carts
                    com.example.ecomm.cart.entity.Cart cart = cartRepository.findById(orderId)
                            .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
                    if (cart.getStatus() != com.example.ecomm.cart.entity.CartStatus.CHECKED_OUT) {
                        throw new ResourceNotFoundException("Order not found: " + orderId);
                    }
                    return toOrderResponseFromCart(toCartResponse(cart));
                });
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> listOrdersByUser(Integer userId) {
        List<OrderResponse> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toOrderResponse)
                .toList();

        List<CartResponse> carts = cartService.listCheckedOutCarts(userId);
        List<OrderResponse> cartOrders = carts.stream()
                .map(this::toOrderResponseFromCart)
                .toList();

        List<OrderResponse> allOrders = new java.util.ArrayList<>(orders);
        allOrders.addAll(cartOrders);
        allOrders.sort((a, b) -> b.createdAt().compareTo(a.createdAt()));

        return allOrders;
    }

    private OrderResponse toOrderResponseFromCart(CartResponse cart) {
        BigDecimal total = cart.items().stream()
                .map(i -> i.priceSnapshot().multiply(BigDecimal.valueOf(i.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<OrderItemResponse> items = cart.items().stream()
                .map(i -> OrderItemResponse.builder()
                        .orderItemId(i.cartItemId())
                        .productId(i.productId())
                        .productName(productServiceClient.getProductName(i.productId()))
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

    private CartResponse toCartResponse(com.example.ecomm.cart.entity.Cart cart) {
        return cartService.getCart(cart.getUserId());
    }

    private OrderResponse toOrderResponse(Order order) {
        List<OrderItemResponse> items = orderItemRepository.findByOrderOrderIdOrderByOrderItemId(order.getOrderId())
                .stream()
                .map(this::toOrderItemResponse)
                .toList();

        // Calculate friendly number if it's missing (e.g. when viewing history)
        if (order.getUserOrderNumber() == null) {
            long count = orderRepository.countByUserIdAndOrderIdLessThanEqual(
                    order.getUserId(),
                    order.getOrderId()
            );
            order.setUserOrderNumber((int) count);
        }

        return OrderResponse.builder()
                .orderId(order.getOrderId())
                .userOrderNumber(order.getUserOrderNumber()) // Map new field
                .userId(order.getUserId())
                .totalAmount(order.getTotalAmount())
                .orderStatus(order.getOrderStatus().name())
                .paymentStatus(order.getPaymentStatus().name())
                .shippingAddress(order.getShippingAddress())
                .createdAt(order.getCreatedAt())
                .items(items)
                .build();
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersForMerchant(Integer merchantId) {
        List<OrderItem> merchantItems = orderItemRepository.findByMerchantIdOrderByOrderCreatedAtDesc(merchantId);

        java.util.Map<Long, Order> ordersMap = new java.util.LinkedHashMap<>();
        for (OrderItem item : merchantItems) {
            ordersMap.putIfAbsent(item.getOrder().getOrderId(), item.getOrder());
        }

        return ordersMap.values().stream()
                .map(this::toOrderResponse)
                .toList();
    }

    private OrderItemResponse toOrderItemResponse(OrderItem item) {
        return OrderItemResponse.builder()
                .orderItemId(item.getOrderItemId())
                .productId(item.getProductId())
                .productName(productServiceClient.getProductName(item.getProductId()))
                .merchantId(item.getMerchantId())
                .quantity(item.getQuantity())
                .price(item.getPrice())
                .build();
    }
}