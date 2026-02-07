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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Tests")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private MerchantRepository merchantRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest customerRegisterRequest;
    private RegisterMerchantRequest merchantRegisterRequest;
    private LoginRequest loginRequest;
    private User testUser;

    @BeforeEach
    void setUp() {
        customerRegisterRequest = new RegisterRequest(
                "customer@example.com",
                "Password@123",
                "John",
                "Doe",
                "9876543210"
        );

        merchantRegisterRequest = new RegisterMerchantRequest(
                "merchant@example.com",
                "Password@123",
                "Jane",
                "Smith",
                "9123456789",
                "Tech Store"
        );

        loginRequest = new LoginRequest(
                "customer@example.com",
                "Password@123"
        );

        testUser = User.builder()
                .id(100L)
                .email("customer@example.com")
                .firstName("John")
                .lastName("Doe")
                .phone("9876543210")
                .role(Role.CUSTOMER)
                .passwordHash("$2a$10$encodedPassword")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("registerCustomer")
    class RegisterCustomerTests {

        @Test
        @DisplayName("should register customer successfully")
        void testRegisterCustomerSuccess() {
            when(userRepository.existsByEmail("customer@example.com"))
                    .thenReturn(false);
            when(passwordEncoder.encode("Password@123"))
                    .thenReturn("$2a$10$encodedPassword");
            when(userRepository.save(any(User.class)))
                    .thenReturn(testUser);
            when(jwtTokenProvider.generateToken(100L, "CUSTOMER"))
                    .thenReturn("jwt.token.here");

            AuthResponse result = authService.registerCustomer(customerRegisterRequest);

            assertThat(result).isNotNull();
            assertThat(result.getToken()).isEqualTo("jwt.token.here");
            assertThat(result.getUserId()).isEqualTo(100L);
            assertThat(result.getEmail()).isEqualTo("customer@example.com");
            assertThat(result.getRole()).isEqualTo(Role.CUSTOMER);

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            User savedUser = userCaptor.getValue();
            assertThat(savedUser.getEmail()).isEqualTo("customer@example.com");
            assertThat(savedUser.getRole()).isEqualTo(Role.CUSTOMER);
        }

        @Test
        @DisplayName("should throw exception when email already exists")
        void testRegisterCustomerEmailExists() {
            when(userRepository.existsByEmail("customer@example.com"))
                    .thenReturn(true);

            assertThatThrownBy(() -> authService.registerCustomer(customerRegisterRequest))
                    .isInstanceOf(AuthenticationException.class)
                    .hasMessageContaining("already exists");
        }

        @Test
        @DisplayName("should set CUSTOMER role for customer registration")
        void testRegisterCustomerSetRole() {
            when(userRepository.existsByEmail("customer@example.com"))
                    .thenReturn(false);
            when(passwordEncoder.encode(anyString()))
                    .thenReturn("$2a$10$encodedPassword");
            when(userRepository.save(any(User.class)))
                    .thenReturn(testUser);
            when(jwtTokenProvider.generateToken(any(Long.class), anyString()))
                    .thenReturn("jwt.token.here");

            authService.registerCustomer(customerRegisterRequest);

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            assertThat(userCaptor.getValue().getRole()).isEqualTo(Role.CUSTOMER);
        }

        @Test
        @DisplayName("should encode password before saving")
        void testRegisterCustomerEncodesPassword() {
            when(userRepository.existsByEmail("customer@example.com"))
                    .thenReturn(false);
            when(passwordEncoder.encode("Password@123"))
                    .thenReturn("$2a$10$encodedPassword");
            when(userRepository.save(any(User.class)))
                    .thenReturn(testUser);
            when(jwtTokenProvider.generateToken(any(Long.class), anyString()))
                    .thenReturn("jwt.token.here");

            authService.registerCustomer(customerRegisterRequest);

            verify(passwordEncoder).encode("Password@123");
        }
    }

    @Nested
    @DisplayName("registerMerchant")
    class RegisterMerchantTests {

        @Test
        @DisplayName("should register merchant successfully")
        void testRegisterMerchantSuccess() {
            User merchantUser = testUser.toBuilder()
                    .email("merchant@example.com")
                    .firstName("Jane")
                    .role(Role.MERCHANT)
                    .id(200L)
                    .build();

            when(userRepository.existsByEmail("merchant@example.com"))
                    .thenReturn(false);
            when(passwordEncoder.encode("Password@123"))
                    .thenReturn("$2a$10$encodedPassword");
            when(userRepository.save(any(User.class)))
                    .thenReturn(merchantUser);
            when(merchantRepository.save(any(Merchant.class)))
                    .thenReturn(Merchant.builder()
                            .merchantId(5L)
                            .name("Tech Store")
                            .user(merchantUser)
                            .build());
            when(jwtTokenProvider.generateToken(200L, "MERCHANT"))
                    .thenReturn("jwt.token.merchant");

            AuthResponse result = authService.registerMerchant(merchantRegisterRequest);

            assertThat(result).isNotNull();
            assertThat(result.getToken()).isEqualTo("jwt.token.merchant");
            assertThat(result.getRole()).isEqualTo(Role.MERCHANT);

            verify(merchantRepository).save(any(Merchant.class));
        }

        @Test
        @DisplayName("should create merchant profile with name")
        void testRegisterMerchantCreatesProfile() {
            User merchantUser = testUser.toBuilder()
                    .email("merchant@example.com")
                    .role(Role.MERCHANT)
                    .id(200L)
                    .build();

            when(userRepository.existsByEmail("merchant@example.com"))
                    .thenReturn(false);
            when(passwordEncoder.encode(anyString()))
                    .thenReturn("$2a$10$encodedPassword");
            when(userRepository.save(any(User.class)))
                    .thenReturn(merchantUser);
            when(merchantRepository.save(any(Merchant.class)))
                    .thenReturn(Merchant.builder()
                            .merchantId(5L)
                            .name("Tech Store")
                            .user(merchantUser)
                            .build());
            when(jwtTokenProvider.generateToken(any(Long.class), anyString()))
                    .thenReturn("jwt.token.here");

            authService.registerMerchant(merchantRegisterRequest);

            ArgumentCaptor<Merchant> merchantCaptor = ArgumentCaptor.forClass(Merchant.class);
            verify(merchantRepository).save(merchantCaptor.capture());
            Merchant savedMerchant = merchantCaptor.getValue();
            assertThat(savedMerchant.getName()).isEqualTo("Tech Store");
        }

        @Test
        @DisplayName("should throw exception when merchant email already exists")
        void testRegisterMerchantEmailExists() {
            when(userRepository.existsByEmail("merchant@example.com"))
                    .thenReturn(true);

            assertThatThrownBy(() -> authService.registerMerchant(merchantRegisterRequest))
                    .isInstanceOf(AuthenticationException.class)
                    .hasMessageContaining("already exists");
        }

        @Test
        @DisplayName("should set MERCHANT role for merchant registration")
        void testRegisterMerchantSetRole() {
            User merchantUser = testUser.toBuilder()
                    .email("merchant@example.com")
                    .role(Role.MERCHANT)
                    .id(200L)
                    .build();

            when(userRepository.existsByEmail("merchant@example.com"))
                    .thenReturn(false);
            when(passwordEncoder.encode(anyString()))
                    .thenReturn("$2a$10$encodedPassword");
            when(userRepository.save(any(User.class)))
                    .thenReturn(merchantUser);
            when(merchantRepository.save(any(Merchant.class)))
                    .thenReturn(Merchant.builder()
                            .merchantId(5L)
                            .user(merchantUser)
                            .build());
            when(jwtTokenProvider.generateToken(any(Long.class), anyString()))
                    .thenReturn("jwt.token.here");

            authService.registerMerchant(merchantRegisterRequest);

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            assertThat(userCaptor.getValue().getRole()).isEqualTo(Role.MERCHANT);
        }
    }

    @Nested
    @DisplayName("login")
    class LoginTests {

        @Test
        @DisplayName("should login successfully with correct credentials")
        void testLoginSuccess() {
            when(userRepository.findByEmail("customer@example.com"))
                    .thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches("Password@123", "$2a$10$encodedPassword"))
                    .thenReturn(true);
            when(jwtTokenProvider.generateToken(100L, "CUSTOMER"))
                    .thenReturn("jwt.token.here");

            AuthResponse result = authService.login(loginRequest);

            assertThat(result).isNotNull();
            assertThat(result.getUserId()).isEqualTo(100L);
            assertThat(result.getEmail()).isEqualTo("customer@example.com");
            assertThat(result.getRole()).isEqualTo(Role.CUSTOMER);
            assertThat(result.getToken()).isNotBlank();
            assertThat(result.getToken()).isEqualTo("jwt.token.here");
        }

        @Test
        @DisplayName("should throw exception when user not found")
        void testLoginUserNotFound() {
            when(userRepository.findByEmail("nonexistent@example.com"))
                    .thenReturn(Optional.empty());

            LoginRequest invalidRequest = new LoginRequest(
                    "nonexistent@example.com",
                    "Password@123"
            );

            assertThatThrownBy(() -> authService.login(invalidRequest))
                    .isInstanceOf(AuthenticationException.class);
        }

        @Test
        @DisplayName("should throw exception when password is incorrect")
        void testLoginIncorrectPassword() {
            when(userRepository.findByEmail("customer@example.com"))
                    .thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches("WrongPassword", "$2a$10$encodedPassword"))
                    .thenReturn(false);

            LoginRequest invalidRequest = new LoginRequest(
                    "customer@example.com",
                    "WrongPassword"
            );

            assertThatThrownBy(() -> authService.login(invalidRequest))
                    .isInstanceOf(AuthenticationException.class)
                    .hasMessageContaining("Invalid email or password");
        }

        @Test
        @DisplayName("should generate JWT token on successful login")
        void testLoginGeneratesToken() {
            when(userRepository.findByEmail("customer@example.com"))
                    .thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches("Password@123", "$2a$10$encodedPassword"))
                    .thenReturn(true);
            when(jwtTokenProvider.generateToken(100L, "CUSTOMER"))
                    .thenReturn("jwt.token.here");

            AuthResponse result = authService.login(loginRequest);

            assertThat(result.getToken()).isNotNull();
            assertThat(result.getToken()).isNotEmpty();
            assertThat(result.getToken()).isEqualTo("jwt.token.here");
        }

        @Test
        @DisplayName("should return correct role on login")
        void testLoginReturnsMerchantRole() {
            User merchantUser = testUser.toBuilder()
                    .role(Role.MERCHANT)
                    .email("merchant@example.com")
                    .id(200L)
                    .build();

            LoginRequest merchantLogin = new LoginRequest(
                    "merchant@example.com",
                    "Password@123"
            );

            when(userRepository.findByEmail("merchant@example.com"))
                    .thenReturn(Optional.of(merchantUser));
            when(passwordEncoder.matches("Password@123", "$2a$10$encodedPassword"))
                    .thenReturn(true);
            when(jwtTokenProvider.generateToken(200L, "MERCHANT"))
                    .thenReturn("jwt.token.merchant");

            AuthResponse result = authService.login(merchantLogin);

            assertThat(result.getRole()).isEqualTo(Role.MERCHANT);
        }
    }
}