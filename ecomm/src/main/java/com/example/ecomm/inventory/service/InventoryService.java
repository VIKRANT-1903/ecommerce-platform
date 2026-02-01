package com.example.ecomm.inventory.service;

import com.example.ecomm.common.exception.InsufficientInventoryException;
import com.example.ecomm.common.exception.ResourceNotFoundException;
import com.example.ecomm.inventory.dto.ConfirmRequest;
import com.example.ecomm.inventory.dto.CreateInventoryRequest;
import com.example.ecomm.inventory.dto.InventoryResponse;
import com.example.ecomm.inventory.dto.ReleaseRequest;
import com.example.ecomm.inventory.dto.ReserveRequest;
import com.example.ecomm.inventory.dto.ReserveResult;
import com.example.ecomm.inventory.dto.UpdateInventoryRequest;
import com.example.ecomm.inventory.entity.Inventory;
import com.example.ecomm.inventory.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryCache inventoryCache;
    private final InventoryLock inventoryLock;

    /**
     * Fetch inventory by product and merchant. Prefers cache, falls back to DB and caches result.
     */
    @Transactional(readOnly = true)
    public InventoryResponse fetch(String productId, Integer merchantId) {
        return inventoryCache.get(productId, merchantId)
                .orElseGet(() -> loadFromDbAndCache(productId, merchantId));
    }

    /**
     * Reserve quantity for checkout. Acquires Redis lock, validates available qty, then updates.
     * Quantity must never go negative.
     */
    public ReserveResult reserve(ReserveRequest request) {
        String productId = request.productId();
        Integer merchantId = request.merchantId();
        int quantity = request.quantity();

        String lockToken = inventoryLock.lock(productId, merchantId);
        try {
            return doReserve(productId, merchantId, quantity);
        } finally {
            inventoryLock.unlock(productId, merchantId, lockToken);
        }
    }

    /**
     * Confirm inventory after payment success. Deducts from reserved only.
     */
    public void confirm(ConfirmRequest request) {
        String productId = request.productId();
        Integer merchantId = request.merchantId();
        int quantity = request.quantity();

        String lockToken = inventoryLock.lock(productId, merchantId);
        try {
            doConfirm(productId, merchantId, quantity);
        } finally {
            inventoryLock.unlock(productId, merchantId, lockToken);
        }
    }

    /**
     * Release reserved quantity on payment failure. Returns reserved to available.
     */
    public void release(ReleaseRequest request) {
        String productId = request.productId();
        Integer merchantId = request.merchantId();
        int quantity = request.quantity();

        String lockToken = inventoryLock.lock(productId, merchantId);
        try {
            doRelease(productId, merchantId, quantity);
        } finally {
            inventoryLock.unlock(productId, merchantId, lockToken);
        }
    }

    private InventoryResponse loadFromDbAndCache(String productId, Integer merchantId) {
        Inventory inv = inventoryRepository.findByProductIdAndMerchantId(productId, merchantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Inventory not found for product " + productId + ", merchant " + merchantId));
        InventoryResponse response = toResponse(inv);
        inventoryCache.put(response);
        return response;
    }

    @Transactional
    protected ReserveResult doReserve(String productId, Integer merchantId, int quantity) {
        Inventory inv = inventoryRepository.findByProductIdAndMerchantIdForUpdate(productId, merchantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Inventory not found for product " + productId + ", merchant " + merchantId));

        int available = inv.getAvailableQty();
        if (available < quantity) {
            return ReserveResult.failure("Insufficient inventory: available=" + available + ", requested=" + quantity);
        }

        inv.setAvailableQty(available - quantity);
        inv.setReservedQty(inv.getReservedQty() + quantity);
        inv.setUpdatedAt(Instant.now());
        inventoryRepository.save(inv);
        inventoryCache.evict(productId, merchantId);
        log.info("Reserved {} for product {} merchant {}", quantity, productId, merchantId);
        return ReserveResult.ok();
    }

    @Transactional
    protected void doConfirm(String productId, Integer merchantId, int quantity) {
        Inventory inv = inventoryRepository.findByProductIdAndMerchantIdForUpdate(productId, merchantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Inventory not found for product " + productId + ", merchant " + merchantId));

        int reserved = inv.getReservedQty();
        if (reserved < quantity) {
            throw new InsufficientInventoryException(
                    "Cannot confirm: reserved=" + reserved + ", requested=" + quantity);
        }

        inv.setReservedQty(reserved - quantity);
        inv.setUpdatedAt(Instant.now());
        inventoryRepository.save(inv);
        inventoryCache.evict(productId, merchantId);
        log.info("Confirmed {} for product {} merchant {}", quantity, productId, merchantId);
    }

    @Transactional
    protected void doRelease(String productId, Integer merchantId, int quantity) {
        Inventory inv = inventoryRepository.findByProductIdAndMerchantIdForUpdate(productId, merchantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Inventory not found for product " + productId + ", merchant " + merchantId));

        int reserved = inv.getReservedQty();
        if (reserved < quantity) {
            log.warn("Release requested {} but reserved only {} for product {} merchant {}; releasing reserved",
                    quantity, reserved, productId, merchantId);
            quantity = reserved;
        }

        inv.setReservedQty(inv.getReservedQty() - quantity);
        inv.setAvailableQty(inv.getAvailableQty() + quantity);
        inv.setUpdatedAt(Instant.now());
        inventoryRepository.save(inv);
        inventoryCache.evict(productId, merchantId);
        log.info("Released {} for product {} merchant {}", quantity, productId, merchantId);
    }

    private static InventoryResponse toResponse(Inventory inv) {
        return InventoryResponse.builder()
                .inventoryId(inv.getInventoryId())
                .productId(inv.getProductId())
                .merchantId(inv.getMerchantId())
                .availableQty(inv.getAvailableQty())
                .reservedQty(inv.getReservedQty())
                .updatedAt(inv.getUpdatedAt())
                .build();
    }

    /**
     * Create new inventory for a product-merchant combination.
     */
    @Transactional
    public InventoryResponse create(CreateInventoryRequest request) {
        String productId = request.productId();
        Integer merchantId = request.merchantId();

        // Check if inventory already exists
        if (inventoryRepository.findByProductIdAndMerchantId(productId, merchantId).isPresent()) {
            throw new IllegalStateException(
                    "Inventory already exists for product " + productId + ", merchant " + merchantId);
        }

        Inventory inv = Inventory.builder()
                .productId(productId)
                .merchantId(merchantId)
                .availableQty(request.availableQty())
                .reservedQty(0)
                .updatedAt(Instant.now())
                .build();

        inv = inventoryRepository.save(inv);
        log.info("Created inventory for product {} merchant {} with qty {}", productId, merchantId, request.availableQty());
        return toResponse(inv);
    }

    /**
     * Update available quantity for existing inventory.
     */
    @Transactional
    public InventoryResponse update(UpdateInventoryRequest request) {
        String productId = request.productId();
        Integer merchantId = request.merchantId();

        String lockToken = inventoryLock.lock(productId, merchantId);
        try {
            Inventory inv = inventoryRepository.findByProductIdAndMerchantIdForUpdate(productId, merchantId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Inventory not found for product " + productId + ", merchant " + merchantId));

            inv.setAvailableQty(request.availableQty());
            inv.setUpdatedAt(Instant.now());
            inventoryRepository.save(inv);
            inventoryCache.evict(productId, merchantId);
            log.info("Updated inventory for product {} merchant {} to qty {}", productId, merchantId, request.availableQty());
            return toResponse(inv);
        } finally {
            inventoryLock.unlock(productId, merchantId, lockToken);
        }
    }

    /**
     * Get all inventory for a merchant.
     */
    @Transactional(readOnly = true)
    public java.util.List<InventoryResponse> findByMerchantId(Integer merchantId) {
        return inventoryRepository.findByMerchantId(merchantId)
                .stream()
                .map(InventoryService::toResponse)
                .toList();
    }
}
