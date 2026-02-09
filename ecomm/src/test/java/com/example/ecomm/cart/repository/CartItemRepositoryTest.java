package com.example.ecomm.cart.repository;

import com.example.ecomm.cart.entity.Cart;
import com.example.ecomm.cart.entity.CartItem;
import com.example.ecomm.cart.entity.CartStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CartItemRepositoryTest {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Cart savedCart;

    @BeforeEach
    void setUp() {
        // We need a Parent Cart first because of foreign key constraints
        Cart cart = Cart.builder()
                .userId(555)
                .status(CartStatus.ACTIVE)
                .updatedAt(Instant.now())
                .build();
        savedCart = entityManager.persist(cart);
    }

    @Test
    @DisplayName("Should find items by Cart ID and order them by Item ID")
    void testFindByCartCartIdOrderByCartItemId() {
        // Arrange
        CartItem item1 = CartItem.builder()
                .cart(savedCart)
                .productId("PROD-A")
                .merchantId(1)
                .quantity(2)
                .priceSnapshot(BigDecimal.TEN)
                .build();

        CartItem item2 = CartItem.builder()
                .cart(savedCart)
                .productId("PROD-B")
                .merchantId(1)
                .quantity(1)
                .priceSnapshot(BigDecimal.valueOf(20))
                .build();

        entityManager.persist(item1);
        entityManager.persist(item2);
        entityManager.flush();

        // Act
        List<CartItem> items = cartItemRepository.findByCartCartIdOrderByCartItemId(savedCart.getCartId());

        // Assert
        assertThat(items).hasSize(2);
        assertThat(items.get(0).getProductId()).isEqualTo("PROD-A"); // First inserted usually has lower ID
    }

    @Test
    @DisplayName("Should find specific item only if it belongs to the specific cart")
    void testFindByCartItemIdAndCartCartId() {
        // Arrange
        CartItem item = CartItem.builder()
                .cart(savedCart)
                .productId("SECRET-PROD")
                .merchantId(99)
                .quantity(1)
                .priceSnapshot(BigDecimal.valueOf(500))
                .build();
        CartItem savedItem = entityManager.persist(item);

        // Act
        Optional<CartItem> result = cartItemRepository.findByCartItemIdAndCartCartId(
                savedItem.getCartItemId(),
                savedCart.getCartId()
        );

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getProductId()).isEqualTo("SECRET-PROD");
    }

    @Test
    @DisplayName("Should NOT find item if cart ID mismatches (Security Check)")
    void testFindByCartItemIdAndCartCartId_Mismatch() {
        // Arrange: Create an item for User 555
        CartItem item = CartItem.builder()
                .cart(savedCart)
                .productId("MY-ITEM")
                .merchantId(1)
                .quantity(1)
                .priceSnapshot(BigDecimal.TEN)
                .build();
        CartItem savedItem = entityManager.persist(item);

        // Act: Try to find this item but using WRONG Cart ID (e.g., 9999)
        Optional<CartItem> result = cartItemRepository.findByCartItemIdAndCartCartId(
                savedItem.getCartItemId(),
                9999L
        );

        // Assert
        assertThat(result).isEmpty();
    }
}