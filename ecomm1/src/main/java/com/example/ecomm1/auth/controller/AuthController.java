package com.example.ecomm1.auth.controller;

import com.example.ecomm1.auth.dto.AuthResponse;
import com.example.ecomm1.auth.dto.LoginRequest;
import com.example.ecomm1.auth.dto.RegisterMerchantRequest;
import com.example.ecomm1.auth.dto.RegisterRequest;
import com.example.ecomm1.auth.service.AuthService;
import com.example.ecomm1.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
@Validated
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register/customer")
    public ResponseEntity<ApiResponse<AuthResponse>> registerCustomer(@Valid @RequestBody RegisterRequest request) {
        log.info("Customer registration attempt for email={}", request.getEmail());
        AuthResponse response = authService.registerCustomer(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response, "Customer registered", "/auth/register/customer"));
    }

    @PostMapping("/register/merchant")
    public ResponseEntity<ApiResponse<AuthResponse>> registerMerchant(@Valid @RequestBody RegisterMerchantRequest request) {
        log.info("Merchant registration attempt for email={}", request.getEmail());
        AuthResponse response = authService.registerMerchant(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response, "Merchant registered", "/auth/register/merchant"));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login attempt for email={}", request.getEmail());
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.ok(response, "Login successful", "/auth/login"));
    }
}
