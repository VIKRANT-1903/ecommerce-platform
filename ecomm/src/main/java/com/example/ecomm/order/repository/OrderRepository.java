package com.example.ecomm.order.repository;

import com.example.ecomm.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserIdOrderByCreatedAtDesc(Integer userId);

    // --- ADD THIS QUERY ---
    // Counts how many orders this user has up to the current orderId.
    // This calculates "Order #1", "Order #2" dynamically.
    long countByUserIdAndOrderIdLessThanEqual(Integer userId, Long orderId);
}