package com.example.ecomm.cart.repository;

import com.example.ecomm.cart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findByCartCartIdOrderByCartItemId(Long cartId);

    Optional<CartItem> findByCartItemIdAndCartCartId(Long cartItemId, Long cartId);
}
