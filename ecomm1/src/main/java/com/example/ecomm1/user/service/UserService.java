package com.example.ecomm1.user.service;

import com.example.ecomm1.common.config.SecurityUtils;
import com.example.ecomm1.merchant.repository.MerchantRepository;
import com.example.ecomm1.user.dto.UpdateUserRequest;
import com.example.ecomm1.user.dto.UserProfileResponse;
import com.example.ecomm1.user.exception.UserNotFoundException;
import com.example.ecomm1.user.model.User;
import com.example.ecomm1.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final MerchantRepository merchantRepository;

    public UserProfileResponse getCurrentUserProfile() {
        Long userId = SecurityUtils.getCurrentUserId();
        log.debug("Fetching profile for userId={}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // Get merchantId if user is a merchant
        Long merchantId = null;
        if (user.getRole().name().equals("MERCHANT")) {
            merchantId = merchantRepository.findByUser_Id(userId)
                    .map(merchant -> merchant.getMerchantId())
                    .orElse(null);
        }

        return UserProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .role(user.getRole())
                .merchantId(merchantId)
                .createdAt(user.getCreatedAt())
                .build();
    }

    @Transactional
    public UserProfileResponse updateCurrentUserProfile(UpdateUserRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("Updating profile for userId={}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }

        user = userRepository.save(user);

        // Get merchantId if user is a merchant
        Long merchantId = null;
        if (user.getRole().name().equals("MERCHANT")) {
            merchantId = merchantRepository.findByUser_Id(userId)
                    .map(merchant -> merchant.getMerchantId())
                    .orElse(null);
        }

        return UserProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .role(user.getRole())
                .merchantId(merchantId)
                .createdAt(user.getCreatedAt())
                .build();
    }
}
