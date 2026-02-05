package com.example.ecomm.cart.service;

import com.example.ecomm.cart.dto.AddCartItemRequest;
import com.example.ecomm.cart.dto.CartItemResponse;
import com.example.ecomm.cart.dto.CartResponse;
import com.example.ecomm.cart.dto.UpdateCartItemRequest;
import com.example.ecomm.cart.entity.Cart;
import com.example.ecomm.cart.entity.CartItem;
import com.example.ecomm.client.OfferServiceClient;
import java.math.BigDecimal;
import com.example.ecomm.cart.entity.CartStatus;
import com.example.ecomm.cart.repository.CartItemRepository;
import com.example.ecomm.cart.repository.CartRepository;
import com.example.ecomm.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final OfferServiceClient offerServiceClient;

    @Transactional
    public CartResponse getOrCreateCart(Integer userId) {
        Cart cart = cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE)
                .orElseGet(() -> createCart(userId));
        return toCartResponse(cart);
    }

    @Transactional
    public CartResponse addItem(Integer userId, AddCartItemRequest request) {
        Cart cart = getOrCreateActiveCartEntity(userId);
        cart.setUpdatedAt(Instant.now());
        BigDecimal price = request.priceSnapshot();
        if (price == null) {
            price = offerServiceClient.getOfferPrice(request.productId(), request.merchantId());
        }
        if (price == null) {
            throw new ResourceNotFoundException("Price not found for product: " + request.productId());
        }

        CartItem item = CartItem.builder()
                .cart(cart)
                .productId(request.productId())
                .merchantId(request.merchantId())
                .quantity(request.quantity())
                .priceSnapshot(price)
                .build();
        cart.getItems().add(item);
        cartItemRepository.save(item);

        return toCartResponse(cart);
    }

    @Transactional
    public CartResponse updateItemQuantity(Integer userId, Long cartItemId, UpdateCartItemRequest request) {
        Cart cart = getOrCreateActiveCartEntity(userId);
        CartItem item = cartItemRepository.findByCartItemIdAndCartCartId(cartItemId, cart.getCartId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found: " + cartItemId));
        item.setQuantity(request.quantity());
        cart.setUpdatedAt(Instant.now());
        return toCartResponse(cart);
    }

    @Transactional
    public CartResponse removeItem(Integer userId, Long cartItemId) {
        Cart cart = getOrCreateActiveCartEntity(userId);
        CartItem item = cartItemRepository.findByCartItemIdAndCartCartId(cartItemId, cart.getCartId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found: " + cartItemId));
        cart.getItems().remove(item);
        cartItemRepository.delete(item);
        cart.setUpdatedAt(Instant.now());
        return toCartResponse(cart);
    }

    @Transactional(readOnly = true)
    public CartResponse getCart(Integer userId) {
        Cart cart = cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("No active cart found for user"));
        return toCartResponse(cart);
    }

    @Transactional
    public void clearCart(Integer userId) {
        Cart cart = cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE)
                .orElse(null);
        if (cart != null) {
            List<CartItem> items = cartItemRepository.findByCartCartIdOrderByCartItemId(cart.getCartId());
            if (!items.isEmpty()) {
                cartItemRepository.deleteAll(items);
                cart.setUpdatedAt(Instant.now());
                log.info("Cleared cart for user {}", userId);
            }
        }
    }

    private Cart getOrCreateActiveCartEntity(Integer userId) {
        return cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE)
                .orElseGet(() -> createCart(userId));
    }

    private Cart createCart(Integer userId) {
        Cart cart = Cart.builder()
                .userId(userId)
                .status(CartStatus.ACTIVE)
                .updatedAt(Instant.now())
                .build();
        return cartRepository.save(cart);
    }

    private CartResponse toCartResponse(Cart cart) {
        List<CartItemResponse> items = cartItemRepository.findByCartCartIdOrderByCartItemId(cart.getCartId())
                .stream()
                .map(this::toCartItemResponse)
                .toList();
        return CartResponse.builder()
                .cartId(cart.getCartId())
                .userId(cart.getUserId())
                .status(cart.getStatus().name())
                .updatedAt(cart.getUpdatedAt())
                .items(items)
                .build();
    }

    private CartItemResponse toCartItemResponse(CartItem item) {
        return CartItemResponse.builder()
                .cartItemId(item.getCartItemId())
                .productId(item.getProductId())
                .merchantId(item.getMerchantId())
                .quantity(item.getQuantity())
                .priceSnapshot(item.getPriceSnapshot())
                .build();
    }

    @Transactional(readOnly = true)
    public java.util.List<CartResponse> listCheckedOutCarts(Integer userId) {
        return cartRepository.findAllByUserIdAndStatus(userId, CartStatus.CHECKED_OUT)
                .stream()
                .map(this::toCartResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CartResponse getCheckedOutCart(Integer userId, Long cartId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found: " + cartId));
        if (!cart.getUserId().equals(userId) || cart.getStatus() != CartStatus.CHECKED_OUT) {
            throw new ResourceNotFoundException("Checked-out cart not found for user: " + cartId);
        }
        return toCartResponse(cart);
    }

    @Transactional
    public CartResponse checkoutCart(Integer userId) {
        Cart cart = cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("No active cart found for user"));
        cart.setStatus(CartStatus.CHECKED_OUT);
        cart.setUpdatedAt(Instant.now());
        Cart saved = cartRepository.save(cart);
        return toCartResponse(saved);
    }
}
