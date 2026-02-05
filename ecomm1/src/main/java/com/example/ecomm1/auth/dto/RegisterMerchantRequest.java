package com.example.ecomm1.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterMerchantRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Password is required")
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
            message = "Password must contain at least 8 characters, one uppercase letter, one lowercase letter, one number and one special character"
    )
    private String password;

    @NotBlank(message = "Merchant name is required")
    @Pattern(
            regexp = "^[A-Za-z0-9 .,&'-]{2,50}$",
            message = "Merchant name can contain letters, numbers, spaces and . , & ' - (2 to 50 characters)"
    )
    private String merchantName;

    @NotBlank(message = "First name is required")
    @Pattern(
            regexp = "^[A-Za-z]{2,30}$",
            message = "First name must contain only letters and be 2 to 30 characters long"
    )
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Pattern(
            regexp = "^[A-Za-z]{2,30}$",
            message = "Last name must contain only letters and be 2 to 30 characters long"
    )
    private String lastName;

    @Pattern(
            regexp = "^\\d{10}$",
            message = "Phone number must be 10 digits"
    )
    private String phone;
}
