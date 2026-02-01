package com.example.ecomm1.merchant.dto;

import com.example.ecomm1.merchant.enums.MerchantStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateMerchantStatusRequest {

    @NotNull(message = "Status is required")
    private MerchantStatus status;
}

