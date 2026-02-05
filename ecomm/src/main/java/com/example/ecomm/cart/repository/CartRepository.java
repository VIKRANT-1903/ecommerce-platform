package com.example.ecomm.cart.repository;

import com.example.ecomm.cart.entity.Cart;
import com.example.ecomm.cart.entity.CartStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByUserIdAndStatus(Integer userId, CartStatus status);

    java.util.List<Cart> findAllByUserIdAndStatus(Integer userId, CartStatus status);
}
