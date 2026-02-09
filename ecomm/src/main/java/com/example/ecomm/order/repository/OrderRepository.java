package com.example.ecomm.order.repository;

import com.example.ecomm.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserIdOrderByCreatedAtDesc(Integer userId);
    /**
     * LOGIC: Count all orders for this user that have an ID smaller than or equal to the current ID.
     * This creates a stable "Row Number" based on the primary key.
     */
    long countByUserIdAndOrderIdLessThanEqual(Integer userId, Long orderId);
}
