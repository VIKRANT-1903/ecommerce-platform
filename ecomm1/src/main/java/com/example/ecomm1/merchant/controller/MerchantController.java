package com.example.ecomm1.merchant.controller;

import com.example.ecomm1.common.dto.ApiResponse;
import com.example.ecomm1.merchant.dto.MerchantProfileResponse;
import com.example.ecomm1.merchant.dto.UpdateMerchantRequest;
import com.example.ecomm1.merchant.dto.UpdateMerchantStatusRequest;
import com.example.ecomm1.merchant.service.MerchantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/merchants")
@RequiredArgsConstructor
@Slf4j
@Validated
public class MerchantController {

    private final MerchantService merchantService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MerchantProfileResponse>> getMe() {
        log.debug("Fetching current merchant profile");
        return ResponseEntity.ok(ApiResponse.ok(merchantService.getCurrentMerchantProfile(), "Merchant profile fetched", "/merchants/me"));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<MerchantProfileResponse>> updateMe(@Valid @RequestBody UpdateMerchantRequest request) {
        log.info("Updating current merchant profile");
        return ResponseEntity.ok(ApiResponse.ok(merchantService.updateCurrentMerchantProfile(request), "Merchant profile updated", "/merchants/me"));
    }

    @PatchMapping("/me/status")
    public ResponseEntity<ApiResponse<MerchantProfileResponse>> updateStatus(@Valid @RequestBody UpdateMerchantStatusRequest request) {
        log.info("Updating current merchant status to {}", request.getStatus());
        return ResponseEntity.ok(ApiResponse.ok(merchantService.updateCurrentMerchantStatus(request), "Merchant status updated", "/merchants/me/status"));
    }
}

