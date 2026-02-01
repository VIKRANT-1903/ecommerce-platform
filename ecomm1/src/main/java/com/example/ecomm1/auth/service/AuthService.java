package com.example.ecomm1.auth.service;

import com.example.ecomm1.auth.config.JwtTokenProvider;
import com.example.ecomm1.auth.dto.AuthResponse;
import com.example.ecomm1.auth.dto.LoginRequest;
import com.example.ecomm1.auth.dto.RegisterMerchantRequest;
import com.example.ecomm1.auth.dto.RegisterRequest;
import com.example.ecomm1.auth.exception.AuthenticationException;
import com.example.ecomm1.merchant.model.Merchant;
import com.example.ecomm1.merchant.repository.MerchantRepository;
import com.example.ecomm1.user.enums.Role;
import com.example.ecomm1.user.model.User;
import com.example.ecomm1.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final MerchantRepository merchantRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public AuthResponse registerCustomer(RegisterRequest request) {
        log.info("Registering customer email={}", request.getEmail());
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AuthenticationException("Email already exists");
        }

        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .role(Role.CUSTOMER)
                .build();

        user = userRepository.save(user);

        String token = jwtTokenProvider.generateToken(user.getId(), user.getRole().name());

        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    @Transactional
    public AuthResponse registerMerchant(RegisterMerchantRequest request) {
        log.info("Registering merchant email={} merchantName={}", request.getEmail(), request.getMerchantName());
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AuthenticationException("Email already exists");
        }

        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .role(Role.MERCHANT)
                .build();

        user = userRepository.save(user);

        Merchant merchant = Merchant.builder()
                .name(request.getMerchantName())
                .user(user)
                .build();

        merchantRepository.save(merchant);

        String token = jwtTokenProvider.generateToken(user.getId(), user.getRole().name());

        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        log.info("Authenticating email={}", request.getEmail());
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AuthenticationException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new AuthenticationException("Invalid email or password");
        }

        String token = jwtTokenProvider.generateToken(user.getId(), user.getRole().name());

        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}
