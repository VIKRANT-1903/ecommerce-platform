package com.example.ecomm.cart.service;

import com.example.ecomm.cart.dto.AddCartItemRequest;
import com.example.ecomm.cart.dto.CartResponse;
import com.example.ecomm.cart.dto.UpdateCartItemRequest;
import com.example.ecomm.cart.entity.Cart;
import com.example.ecomm.cart.entity.CartItem;
import com.example.ecomm.cart.entity.CartStatus;
import com.example.ecomm.cart.repository.CartItemRepository;
import com.example.ecomm.cart.repository.CartRepository;
import com.example.ecomm.client.OfferServiceClient;
import com.example.ecomm.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private OfferServiceClient offerServiceClient;

    @InjectMocks
    private CartService cartService;

    private Integer userId = 101;
    private Long cartId = 500L;
    private Cart activeCart;
    private CartItem cartItem;

    @BeforeEach
    void setUp() {
        // Setup a standard Active Cart for reuse
        activeCart = Cart.builder()
                .cartId(cartId)
                .userId(userId)
                .status(CartStatus.ACTIVE)
                .updatedAt(Instant.now())
                .items(new ArrayList<>())
                .build();

        // Setup a standard Cart Item
        cartItem = CartItem.builder()
                .cartItemId(1L)
                .cart(activeCart)
                .productId("PROD-001")
                .merchantId(10)
                .quantity(1)
                .priceSnapshot(new BigDecimal("100.00"))
                .build();
    }

    // ========================================================================
    // 1. Tests for getOrCreateCart
    // ========================================================================

    @Test
    void testGetOrCreateCart_Existing() {
        // Arrange
        when(cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE))
                .thenReturn(Optional.of(activeCart));

        // Stub the item fetch that happens inside toCartResponse()
        when(cartItemRepository.findByCartCartIdOrderByCartItemId(cartId))
                .thenReturn(List.of());

        // Act
        CartResponse response = cartService.getOrCreateCart(userId);

        // Assert
        assertNotNull(response);
        assertEquals(cartId, response.cartId());
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void testGetOrCreateCart_New() {
        // Arrange
        when(cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE))
                .thenReturn(Optional.empty()); // No cart found

        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> {
            Cart c = invocation.getArgument(0);
            c.setCartId(cartId); // Simulate DB generating ID
            return c;
        });

        // Act
        CartResponse response = cartService.getOrCreateCart(userId);

        // Assert
        assertNotNull(response);
        assertEquals(userId, response.userId());
        verify(cartRepository).save(any(Cart.class));
    }

    // ========================================================================
    // 2. Tests for addItem (Including OfferService logic)
    // ========================================================================

    @Test
    void testAddItem_WithPriceInRequest() {
        // Arrange
        AddCartItemRequest request = AddCartItemRequest.builder()
                .productId("P1")
                .merchantId(1)
                .quantity(2)
                .priceSnapshot(new BigDecimal("50.00")) // Price provided
                .build();

        when(cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE))
                .thenReturn(Optional.of(activeCart));

        // Act
        cartService.addItem(userId, request);

        // Assert
        // 1. Verify OfferService was NOT called because price was in request
        verify(offerServiceClient, never()).getOfferPrice(anyString(), anyInt());

        // 2. Verify item was saved
        verify(cartItemRepository).save(any(CartItem.class));
    }

    @Test
    void testAddItem_FetchPriceFromService() {
        // Arrange
        AddCartItemRequest request = AddCartItemRequest.builder()
                .productId("P1")
                .merchantId(1)
                .quantity(2)
                .priceSnapshot(null) // Price MISSING
                .build();

        when(cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE))
                .thenReturn(Optional.of(activeCart));

        // Mock the Feign Client to return a price
        when(offerServiceClient.getOfferPrice("P1", 1))
                .thenReturn(new BigDecimal("99.99"));

        // Act
        cartService.addItem(userId, request);

        // Assert
        verify(offerServiceClient).getOfferPrice("P1", 1);
        verify(cartItemRepository).save(any(CartItem.class));
    }

    @Test
    void testAddItem_PriceNotFound_ThrowsException() {
        // Arrange
        AddCartItemRequest request = AddCartItemRequest.builder()
                .productId("P1")
                .merchantId(1)
                .quantity(1)
                .build(); // Price null

        when(cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE))
                .thenReturn(Optional.of(activeCart));

        // Mock Feign Client returning null (no price found)
        when(offerServiceClient.getOfferPrice(anyString(), anyInt())).thenReturn(null);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                cartService.addItem(userId, request)
        );
    }

    // ========================================================================
    // 3. Tests for updateItemQuantity
    // ========================================================================

    @Test
    void testUpdateItemQuantity_Success() {
        // Arrange
        Long itemId = 1L;
        UpdateCartItemRequest request = UpdateCartItemRequest.builder().quantity(10).build();

        // Mock Cart and Item
        when(cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE))
                .thenReturn(Optional.of(activeCart));

        when(cartItemRepository.findByCartItemIdAndCartCartId(itemId, cartId))
                .thenReturn(Optional.of(cartItem)); // cartItem has quantity 1 initially

        // Act
        cartService.updateItemQuantity(userId, itemId, request);

        // Assert
        // NOTE: Your code for updateItemQuantity does NOT call repository.save().
        // It relies on Transactional Dirty Checking.
        // So we verify the OBJECT state changed, but we do NOT verify repository.save().
        assertEquals(10, cartItem.getQuantity());
    }

    // ========================================================================
    // 4. Tests for removeItem
    // ========================================================================

    @Test
    void testRemoveItem_Success() {
        // Arrange
        Long itemId = 1L;
        // Add item to the cart list so we can verify removal
        activeCart.getItems().add(cartItem);

        when(cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE))
                .thenReturn(Optional.of(activeCart));
        when(cartItemRepository.findByCartItemIdAndCartCartId(itemId, cartId))
                .thenReturn(Optional.of(cartItem));

        // Act
        cartService.removeItem(userId, itemId);

        // Assert
        verify(cartItemRepository).delete(cartItem);
        assertFalse(activeCart.getItems().contains(cartItem)); // Verify removed from list
    }

    // ========================================================================
    // 5. Tests for Checkout
    // ========================================================================

    @Test
    void testCheckoutCart_Success() {
        // Arrange
        when(cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE))
                .thenReturn(Optional.of(activeCart));

        when(cartRepository.save(any(Cart.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        CartResponse response = cartService.checkoutCart(userId);

        // Assert
        assertEquals("CHECKED_OUT", response.status());
        verify(cartRepository).save(activeCart);
    }
}