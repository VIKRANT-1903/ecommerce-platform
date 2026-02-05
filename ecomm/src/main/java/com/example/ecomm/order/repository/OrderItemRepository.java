package com.example.ecomm.order.repository;

import com.example.ecomm.order.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrderOrderIdOrderByOrderItemId(Long orderId);
    
    List<OrderItem> findByMerchantIdOrderByOrderCreatedAtDesc(Integer merchantId);
}
