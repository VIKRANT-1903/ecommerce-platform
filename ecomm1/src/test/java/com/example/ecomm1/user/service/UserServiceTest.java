package com.example.ecomm1.user.service;

import com.example.ecomm1.common.config.SecurityUtils;
import com.example.ecomm1.merchant.model.Merchant;
import com.example.ecomm1.merchant.repository.MerchantRepository;
import com.example.ecomm1.user.dto.UpdateUserRequest;
import com.example.ecomm1.user.dto.UserProfileResponse;
import com.example.ecomm1.user.exception.UserNotFoundException;
import com.example.ecomm1.user.model.Role;
import com.example.ecomm1.user.model.User;
import com.example.ecomm1.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private MerchantRepository merchantRepository;

    @InjectMocks
    private UserService userService;

    private User testCustomer;
    private User testMerchant;
    private Merchant merchantProfile;

    @BeforeEach
    void setUp() {
        testCustomer = User.builder()
                .id(100L)
                .email("customer@example.com")
                .firstName("John")
                .lastName("Doe")
                .phone("9876543210")
                .role(Role.CUSTOMER)
                .createdAt(LocalDateTime.now())
                .build();

        testMerchant = User.builder()
                .id(200L)
                .email("merchant@example.com")
                .firstName("Jane")
                .lastName("Smith")
                .phone("9123456789")
                .role(Role.MERCHANT)
                .createdAt(LocalDateTime.now())
                .build();

        merchantProfile = Merchant.builder()
                .merchantId(5L)
                .name("Tech Store")
                .user(200L)
                .rating(4.5)
                .build();
    }

    @Nested
    @DisplayName("getCurrentUserProfile")
    class GetCurrentUserProfileTests {

        @Test
        @DisplayName("should return customer profile without merchantId")
        void testGetCustomerProfile() {
            try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getCurrentUserId).thenReturn(100L);
                when(userRepository.findById(100L)).thenReturn(Optional.of(testCustomer));

                UserProfileResponse result = userService.getCurrentUserProfile();

                assertThat(result).isNotNull();
                assertThat(result.id()).isEqualTo(100L);
                assertThat(result.email()).isEqualTo("customer@example.com");
                assertThat(result.role()).isEqualTo(Role.CUSTOMER);
                assertThat(result.merchantId()).isNull();
            }
        }

        @Test
        @DisplayName("should return merchant profile with merchantId")
        void testGetMerchantProfile() {
            try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getCurrentUserId).thenReturn(200L);
                when(userRepository.findById(200L)).thenReturn(Optional.of(testMerchant));
                when(merchantRepository.findByUser_Id(200L)).thenReturn(Optional.of(merchantProfile));

                UserProfileResponse result = userService.getCurrentUserProfile();

                assertThat(result).isNotNull();
                assertThat(result.id()).isEqualTo(200L);
                assertThat(result.email()).isEqualTo("merchant@example.com");
                assertThat(result.role()).isEqualTo(Role.MERCHANT);
                assertThat(result.merchantId()).isEqualTo(5L);
            }
        }

        @Test
        @DisplayName("should throw UserNotFoundException when user not found")
        void testGetProfileUserNotFound() {
            try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getCurrentUserId).thenReturn(999L);
                when(userRepository.findById(999L)).thenReturn(Optional.empty());

                assertThatThrownBy(() -> userService.getCurrentUserProfile())
                        .isInstanceOf(UserNotFoundException.class);
            }
        }

        @Test
        @DisplayName("should include all user fields in profile")
        void testGetProfileIncludesAllFields() {
            try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getCurrentUserId).thenReturn(100L);
                when(userRepository.findById(100L)).thenReturn(Optional.of(testCustomer));

                UserProfileResponse result = userService.getCurrentUserProfile();

                assertThat(result)
                        .extracting("firstName", "lastName", "phone", "email")
                        .containsExactly("John", "Doe", "9876543210", "customer@example.com");
            }
        }
    }

    @Nested
    @DisplayName("updateCurrentUserProfile")
    class UpdateCurrentUserProfileTests {
        @Test
        @DisplayName("should update user profile successfully")
        void testUpdateProfile() {
            try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getCurrentUserId).thenReturn(100L);
                when(userRepository.findById(100L)).thenReturn(Optional.of(testCustomer));
                when(userRepository.save(any(User.class))).thenReturn(testCustomer);

                UpdateUserRequest request = new UpdateUserRequest(
                        "Jonathan",  // firstName
                        "Doe",       // lastName
                        "9876543210" // phone
                );

                UserProfileResponse result = userService.updateCurrentUserProfile(request);

                assertThat(result).isNotNull();
                assertThat(result.id()).isEqualTo(100L);
                verify(userRepository).save(any(User.class));
            }
        }

        @Test
        @DisplayName("should preserve merchant ID during profile update")
        void testUpdateMerchantProfilePreservesMerchantId() {
            try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getCurrentUserId).thenReturn(200L);
                when(userRepository.findById(200L)).thenReturn(Optional.of(testMerchant));
                when(userRepository.save(any(User.class))).thenReturn(testMerchant);
                when(merchantRepository.findByUser_Id(200L)).thenReturn(Optional.of(merchantProfile));

                UpdateUserRequest request = new UpdateUserRequest(
                        "Janet", // firstName
                        null,    // lastName (unchanged)
                        null     // phone (unchanged)
                );

                UserProfileResponse result = userService.updateCurrentUserProfile(request);

                assertThat(result.merchantId()).isEqualTo(5L);
            }
        }

        @Test
        @DisplayName("should throw when user not found during update")
        void testUpdateProfileUserNotFound() {
            try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getCurrentUserId).thenReturn(999L);
                when(userRepository.findById(999L)).thenReturn(Optional.empty());

                UpdateUserRequest request = new UpdateUserRequest("Test", null, null);

                assertThatThrownBy(() -> userService.updateCurrentUserProfile(request))
                        .isInstanceOf(UserNotFoundException.class);
            }
        }

        }
    }
}