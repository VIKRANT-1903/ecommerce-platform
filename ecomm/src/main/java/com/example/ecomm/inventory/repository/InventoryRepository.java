package com.example.ecomm.inventory.repository;

import com.example.ecomm.inventory.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    Optional<Inventory> findByProductIdAndMerchantId(String productId, Integer merchantId);

    List<Inventory> findByMerchantId(Integer merchantId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Inventory i WHERE i.productId = :productId AND i.merchantId = :merchantId")
    Optional<Inventory> findByProductIdAndMerchantIdForUpdate(
            @Param("productId") String productId,
            @Param("merchantId") Integer merchantId
    );
}
