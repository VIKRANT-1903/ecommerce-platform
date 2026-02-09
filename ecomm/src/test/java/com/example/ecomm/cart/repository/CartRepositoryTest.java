package com.example.ecomm.cart.repository;

import com.example.ecomm.cart.entity.Cart;
import com.example.ecomm.cart.entity.CartStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CartRepositoryTest {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("Should find active cart for user")
    void testFindByUserIdAndStatus_Active() {
        // Arrange
        Cart activeCart = Cart.builder()
                .userId(101)
                .status(CartStatus.ACTIVE)
                .updatedAt(Instant.now())
                .build();
        entityManager.persist(activeCart);

        Cart checkedOutCart = Cart.builder()
                .userId(101)
                .status(CartStatus.CHECKED_OUT)
                .updatedAt(Instant.now())
                .build();
        entityManager.persist(checkedOutCart);

        entityManager.flush(); // Force write to DB

        // Act
        Optional<Cart> foundCart = cartRepository.findByUserIdAndStatus(101, CartStatus.ACTIVE);

        // Assert
        assertThat(foundCart).isPresent();
        assertThat(foundCart.get().getStatus()).isEqualTo(CartStatus.ACTIVE);
    }

    @Test
    @DisplayName("Should find all checked-out carts for history")
    void testFindAllByUserIdAndStatus() {
        // Arrange
        Cart cart1 = Cart.builder().userId(202).status(CartStatus.CHECKED_OUT).updatedAt(Instant.now()).build();
        Cart cart2 = Cart.builder().userId(202).status(CartStatus.CHECKED_OUT).updatedAt(Instant.now()).build();
        Cart active = Cart.builder().userId(202).status(CartStatus.ACTIVE).updatedAt(Instant.now()).build();

        entityManager.persist(cart1);
        entityManager.persist(cart2);
        entityManager.persist(active);

        // Act
        List<Cart> history = cartRepository.findAllByUserIdAndStatus(202, CartStatus.CHECKED_OUT);

        // Assert
        assertThat(history).hasSize(2);
        assertThat(history).extracting(Cart::getStatus).containsOnly(CartStatus.CHECKED_OUT);
    }
}