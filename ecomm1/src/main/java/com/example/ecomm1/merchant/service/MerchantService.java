package com.example.ecomm1.merchant.service;

import com.example.ecomm1.common.config.SecurityUtils;
import com.example.ecomm1.merchant.dto.MerchantProfileResponse;
import com.example.ecomm1.merchant.dto.UpdateMerchantRequest;
import com.example.ecomm1.merchant.dto.UpdateMerchantStatusRequest;
import com.example.ecomm1.merchant.enums.MerchantStatus;
import com.example.ecomm1.merchant.exception.MerchantNotFoundException;
import com.example.ecomm1.merchant.model.Merchant;
import com.example.ecomm1.merchant.repository.MerchantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MerchantService {

    private final MerchantRepository merchantRepository;

    public MerchantProfileResponse getCurrentMerchantProfile() {
        SecurityUtils.requireRole("MERCHANT");
        Long userId = SecurityUtils.getCurrentUserId();
        log.debug("Fetching merchant profile for userId={}", userId);

        Merchant merchant = merchantRepository.findByUser_Id(userId)
                .orElseThrow(() -> new MerchantNotFoundException("Merchant not found"));

        return toProfileResponse(merchant);
    }

    @Transactional
    public MerchantProfileResponse updateCurrentMerchantProfile(UpdateMerchantRequest request) {
        SecurityUtils.requireRole("MERCHANT");
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("Updating merchant profile for userId={}", userId);

        Merchant merchant = merchantRepository.findByUser_Id(userId)
                .orElseThrow(() -> new MerchantNotFoundException("Merchant not found"));

        merchant.setName(request.getName());

        Merchant saved = merchantRepository.save(merchant);
        return toProfileResponse(saved);
    }

    @Transactional
    public MerchantProfileResponse updateCurrentMerchantStatus(UpdateMerchantStatusRequest request) {
        SecurityUtils.requireRole("MERCHANT");
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("Updating merchant status for userId={} status={}", userId, request.getStatus());

        Merchant merchant = merchantRepository.findByUser_Id(userId)
                .orElseThrow(() -> new MerchantNotFoundException("Merchant not found"));

        MerchantStatus status = request.getStatus();
        if (status != MerchantStatus.ACTIVE && status != MerchantStatus.INACTIVE) {
            throw new IllegalArgumentException("Status must be ACTIVE or INACTIVE");
        }

        merchant.setStatus(status);
        Merchant saved = merchantRepository.save(merchant);
        return toProfileResponse(saved);
    }

    private MerchantProfileResponse toProfileResponse(Merchant merchant) {
        return MerchantProfileResponse.builder()
                .merchantId(merchant.getMerchantId())
                .name(merchant.getName())
                .rating(merchant.getRating())
                .status(merchant.getStatus())
                .createdAt(merchant.getCreatedAt())
                .build();
    }
}

