package com.example.ecomm1.auth.dto;

import com.example.ecomm1.user.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder.Default;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private String token;
    private Long userId;
    private String email;
    private Role role;
    @Default
    private String type = "Bearer";
}
