package com.example.ecomm.inventory.controller;

import com.example.ecomm.common.response.ApiResponse;
import com.example.ecomm.inventory.dto.ConfirmRequest;
import com.example.ecomm.inventory.dto.CreateInventoryRequest;
import com.example.ecomm.inventory.dto.InventoryResponse;
import com.example.ecomm.inventory.dto.ReleaseRequest;
import com.example.ecomm.inventory.dto.ReserveRequest;
import com.example.ecomm.inventory.dto.ReserveResult;
import com.example.ecomm.inventory.dto.UpdateInventoryRequest;
import com.example.ecomm.inventory.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    /**
     * Fetch inventory by product and merchant. Prefers cache, then DB.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<InventoryResponse>> fetch(
            @RequestParam String productId,
            @RequestParam Integer merchantId) {
        InventoryResponse response = inventoryService.fetch(productId, merchantId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Reserve quantity for checkout. Uses Redis lock to prevent overselling.
     */
    @PostMapping("/reserve")
    public ResponseEntity<ApiResponse<ReserveResult>> reserve(@Valid @RequestBody ReserveRequest request) {
        ReserveResult result = inventoryService.reserve(request);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * Confirm inventory after payment success. Deducts from reserved.
     */
    @PostMapping("/confirm")
    public ResponseEntity<ApiResponse<Void>> confirm(@Valid @RequestBody ConfirmRequest request) {
        inventoryService.confirm(request);
        return ResponseEntity.ok(ApiResponse.success(null, "Confirmed"));
    }

    /**
     * Release reserved quantity on payment failure. Returns to available.
     */
    @PostMapping("/release")
    public ResponseEntity<ApiResponse<Void>> release(@Valid @RequestBody ReleaseRequest request) {
        inventoryService.release(request);
        return ResponseEntity.ok(ApiResponse.success(null, "Released"));
    }

    /**
     * Create new inventory for a product-merchant combination.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<InventoryResponse>> create(@Valid @RequestBody CreateInventoryRequest request) {
        InventoryResponse response = inventoryService.create(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Inventory created"));
    }

    /**
     * Update available quantity for existing inventory.
     */
    @PutMapping
    public ResponseEntity<ApiResponse<InventoryResponse>> update(@Valid @RequestBody UpdateInventoryRequest request) {
        InventoryResponse response = inventoryService.update(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Inventory updated"));
    }

    /**
     * Get all inventory for a merchant.
     */
    @GetMapping("/merchant")
    public ResponseEntity<ApiResponse<List<InventoryResponse>>> getByMerchant(@RequestParam Integer merchantId) {
        List<InventoryResponse> response = inventoryService.findByMerchantId(merchantId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
