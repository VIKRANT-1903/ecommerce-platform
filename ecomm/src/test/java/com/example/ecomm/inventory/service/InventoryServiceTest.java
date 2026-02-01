package com.example.ecomm.inventory.service;

import com.example.ecomm.common.exception.InsufficientInventoryException;
import com.example.ecomm.common.exception.ResourceNotFoundException;
import com.example.ecomm.inventory.dto.ConfirmRequest;
import com.example.ecomm.inventory.dto.InventoryResponse;
import com.example.ecomm.inventory.dto.ReleaseRequest;
import com.example.ecomm.inventory.dto.ReserveRequest;
import com.example.ecomm.inventory.dto.ReserveResult;
import com.example.ecomm.inventory.entity.Inventory;
import com.example.ecomm.inventory.repository.InventoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    private static final String PRODUCT_ID = "P1";
    private static final Integer MERCHANT_ID = 1;
    private static final String LOCK_TOKEN = "lock-token";

    @Mock
    private InventoryRepository inventoryRepository;
    @Mock
    private InventoryCache inventoryCache;
    @Mock
    private InventoryLock inventoryLock;

    @InjectMocks
    private InventoryService inventoryService;

    private Inventory inventory;

    @BeforeEach
    void setUp() {
        inventory = Inventory.builder()
                .inventoryId(1L)
                .productId(PRODUCT_ID)
                .merchantId(MERCHANT_ID)
                .availableQty(10)
                .reservedQty(0)
                .updatedAt(Instant.now())
                .build();
    }

    @Nested
    @DisplayName("fetch")
    class Fetch {

        @Test
        @DisplayName("returns from cache when present")
        void returnsFromCache() {
            InventoryResponse cached = InventoryResponse.builder()
                    .inventoryId(1L)
                    .productId(PRODUCT_ID)
                    .merchantId(MERCHANT_ID)
                    .availableQty(10)
                    .reservedQty(0)
                    .updatedAt(Instant.now())
                    .build();
            when(inventoryCache.get(PRODUCT_ID, MERCHANT_ID)).thenReturn(Optional.of(cached));

            InventoryResponse result = inventoryService.fetch(PRODUCT_ID, MERCHANT_ID);

            assertThat(result).isEqualTo(cached);
            verify(inventoryCache).get(PRODUCT_ID, MERCHANT_ID);
        }

        @Test
        @DisplayName("loads from DB and caches when cache miss")
        void loadsFromDbOnCacheMiss() {
            when(inventoryCache.get(PRODUCT_ID, MERCHANT_ID)).thenReturn(Optional.empty());
            when(inventoryRepository.findByProductIdAndMerchantId(PRODUCT_ID, MERCHANT_ID))
                    .thenReturn(Optional.of(inventory));

            InventoryResponse result = inventoryService.fetch(PRODUCT_ID, MERCHANT_ID);

            assertThat(result.productId()).isEqualTo(PRODUCT_ID);
            assertThat(result.merchantId()).isEqualTo(MERCHANT_ID);
            assertThat(result.availableQty()).isEqualTo(10);
            assertThat(result.reservedQty()).isEqualTo(0);
            verify(inventoryCache).put(result);
        }

        @Test
        @DisplayName("throws when not found in DB")
        void throwsWhenNotFound() {
            when(inventoryCache.get(PRODUCT_ID, MERCHANT_ID)).thenReturn(Optional.empty());
            when(inventoryRepository.findByProductIdAndMerchantId(PRODUCT_ID, MERCHANT_ID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> inventoryService.fetch(PRODUCT_ID, MERCHANT_ID))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining(PRODUCT_ID);
        }
    }

    @Nested
    @DisplayName("reserve")
    class Reserve {

        @Test
        @DisplayName("returns success when enough available")
        void successWhenEnoughAvailable() {
            ReserveRequest request = ReserveRequest.builder()
                    .productId(PRODUCT_ID)
                    .merchantId(MERCHANT_ID)
                    .quantity(3)
                    .build();
            when(inventoryLock.lock(PRODUCT_ID, MERCHANT_ID)).thenReturn(LOCK_TOKEN);
            when(inventoryRepository.findByProductIdAndMerchantIdForUpdate(PRODUCT_ID, MERCHANT_ID))
                    .thenReturn(Optional.of(inventory));

            ReserveResult result = inventoryService.reserve(request);

            assertThat(result.success()).isTrue();
            assertThat(inventory.getAvailableQty()).isEqualTo(7);
            assertThat(inventory.getReservedQty()).isEqualTo(3);
            verify(inventoryRepository).save(inventory);
            verify(inventoryCache).evict(PRODUCT_ID, MERCHANT_ID);
            verify(inventoryLock).unlock(PRODUCT_ID, MERCHANT_ID, LOCK_TOKEN);
        }

        @Test
        @DisplayName("returns failure when insufficient available")
        void failureWhenInsufficient() {
            ReserveRequest request = ReserveRequest.builder()
                    .productId(PRODUCT_ID)
                    .merchantId(MERCHANT_ID)
                    .quantity(100)
                    .build();
            when(inventoryLock.lock(PRODUCT_ID, MERCHANT_ID)).thenReturn(LOCK_TOKEN);
            when(inventoryRepository.findByProductIdAndMerchantIdForUpdate(PRODUCT_ID, MERCHANT_ID))
                    .thenReturn(Optional.of(inventory));

            ReserveResult result = inventoryService.reserve(request);

            assertThat(result.success()).isFalse();
            assertThat(result.message()).contains("Insufficient inventory");
            assertThat(inventory.getAvailableQty()).isEqualTo(10);
            assertThat(inventory.getReservedQty()).isEqualTo(0);
            verify(inventoryLock).unlock(PRODUCT_ID, MERCHANT_ID, LOCK_TOKEN);
        }

        @Test
        @DisplayName("throws when inventory not found")
        void throwsWhenNotFound() {
            ReserveRequest request = ReserveRequest.builder()
                    .productId(PRODUCT_ID)
                    .merchantId(MERCHANT_ID)
                    .quantity(1)
                    .build();
            when(inventoryLock.lock(PRODUCT_ID, MERCHANT_ID)).thenReturn(LOCK_TOKEN);
            when(inventoryRepository.findByProductIdAndMerchantIdForUpdate(PRODUCT_ID, MERCHANT_ID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> inventoryService.reserve(request))
                    .isInstanceOf(ResourceNotFoundException.class);
            verify(inventoryLock).unlock(PRODUCT_ID, MERCHANT_ID, LOCK_TOKEN);
        }
    }

    @Nested
    @DisplayName("confirm")
    class Confirm {

        @Test
        @DisplayName("deducts from reserved")
        void deductsReserved() {
            inventory.setReservedQty(5);
            ConfirmRequest request = ConfirmRequest.builder()
                    .productId(PRODUCT_ID)
                    .merchantId(MERCHANT_ID)
                    .quantity(2)
                    .build();
            when(inventoryLock.lock(PRODUCT_ID, MERCHANT_ID)).thenReturn(LOCK_TOKEN);
            when(inventoryRepository.findByProductIdAndMerchantIdForUpdate(PRODUCT_ID, MERCHANT_ID))
                    .thenReturn(Optional.of(inventory));

            inventoryService.confirm(request);

            assertThat(inventory.getReservedQty()).isEqualTo(3);
            verify(inventoryRepository).save(inventory);
            verify(inventoryCache).evict(PRODUCT_ID, MERCHANT_ID);
            verify(inventoryLock).unlock(PRODUCT_ID, MERCHANT_ID, LOCK_TOKEN);
        }

        @Test
        @DisplayName("throws when reserved less than requested")
        void throwsWhenInsufficientReserved() {
            inventory.setReservedQty(1);
            ConfirmRequest request = ConfirmRequest.builder()
                    .productId(PRODUCT_ID)
                    .merchantId(MERCHANT_ID)
                    .quantity(2)
                    .build();
            when(inventoryLock.lock(PRODUCT_ID, MERCHANT_ID)).thenReturn(LOCK_TOKEN);
            when(inventoryRepository.findByProductIdAndMerchantIdForUpdate(PRODUCT_ID, MERCHANT_ID))
                    .thenReturn(Optional.of(inventory));

            assertThatThrownBy(() -> inventoryService.confirm(request))
                    .isInstanceOf(InsufficientInventoryException.class)
                    .hasMessageContaining("Cannot confirm");
            verify(inventoryLock).unlock(PRODUCT_ID, MERCHANT_ID, LOCK_TOKEN);
        }
    }

    @Nested
    @DisplayName("release")
    class Release {

        @Test
        @DisplayName("returns reserved to available")
        void returnsToAvailable() {
            inventory.setReservedQty(4);
            inventory.setAvailableQty(6);
            ReleaseRequest request = ReleaseRequest.builder()
                    .productId(PRODUCT_ID)
                    .merchantId(MERCHANT_ID)
                    .quantity(2)
                    .build();
            when(inventoryLock.lock(PRODUCT_ID, MERCHANT_ID)).thenReturn(LOCK_TOKEN);
            when(inventoryRepository.findByProductIdAndMerchantIdForUpdate(PRODUCT_ID, MERCHANT_ID))
                    .thenReturn(Optional.of(inventory));

            inventoryService.release(request);

            assertThat(inventory.getReservedQty()).isEqualTo(2);
            assertThat(inventory.getAvailableQty()).isEqualTo(8);
            verify(inventoryRepository).save(inventory);
            verify(inventoryCache).evict(PRODUCT_ID, MERCHANT_ID);
            verify(inventoryLock).unlock(PRODUCT_ID, MERCHANT_ID, LOCK_TOKEN);
        }

        @Test
        @DisplayName("caps release to reserved when requested more")
        void capsReleaseToReserved() {
            inventory.setReservedQty(2);
            inventory.setAvailableQty(8);
            ReleaseRequest request = ReleaseRequest.builder()
                    .productId(PRODUCT_ID)
                    .merchantId(MERCHANT_ID)
                    .quantity(5)
                    .build();
            when(inventoryLock.lock(PRODUCT_ID, MERCHANT_ID)).thenReturn(LOCK_TOKEN);
            when(inventoryRepository.findByProductIdAndMerchantIdForUpdate(PRODUCT_ID, MERCHANT_ID))
                    .thenReturn(Optional.of(inventory));

            inventoryService.release(request);

            assertThat(inventory.getReservedQty()).isEqualTo(0);
            assertThat(inventory.getAvailableQty()).isEqualTo(10);
            verify(inventoryLock).unlock(PRODUCT_ID, MERCHANT_ID, LOCK_TOKEN);
        }
    }
}
