package com.example.ecomm1.merchant.dto;

import com.example.ecomm1.merchant.enums.MerchantStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MerchantProfileResponse {
    private Long merchantId;
    private String name;
    private Double rating;
    private MerchantStatus status;
    private OffsetDateTime createdAt;
}

