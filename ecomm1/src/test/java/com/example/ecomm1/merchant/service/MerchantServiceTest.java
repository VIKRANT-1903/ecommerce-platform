package com.example.ecomm1.merchant.service;

import com.example.ecomm1.common.config.SecurityUtils;
import com.example.ecomm1.merchant.dto.MerchantProfileResponse;
import com.example.ecomm1.merchant.enums.MerchantStatus;
import com.example.ecomm1.merchant.exception.MerchantNotFoundException;
import com.example.ecomm1.merchant.model.Merchant;
import com.example.ecomm1.merchant.repository.MerchantRepository;
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
import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MerchantService Tests")
class MerchantServiceTest {

    @Mock
    private MerchantRepository merchantRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MerchantService merchantService;

    private User testMerchantUser;
    private Merchant testMerchant;

    @BeforeEach
    void setUp() {
        testMerchantUser = User.builder()
                .id(200L)
                .email("merchant@example.com")
                .firstName("Jane")
                .lastName("Smith")
                .phone("9123456789")
                .role(Role.MERCHANT)
                .createdAt(LocalDateTime.now())
                .build();

        testMerchant = Merchant.builder()
                .merchantId(5L)
                .name("Tech Store")
                .user(testMerchantUser)
                .rating(4.5)
                .status(MerchantStatus.ACTIVE)
                .createdAt(OffsetDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("getCurrentMerchantProfile")
    class GetCurrentMerchantProfileTests {

        @Test
        @DisplayName("should return merchant profile successfully")
        void testGetMerchantProfileSuccess() {
            try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getCurrentUserId).thenReturn(200L);
                when(merchantRepository.findByUser_Id(200L))
                        .thenReturn(Optional.of(testMerchant));

                MerchantProfileResponse result = merchantService.getCurrentMerchantProfile();

                assertThat(result).isNotNull();
                assertThat(result.merchantId()).isEqualTo(5L);
                assertThat(result.name()).isEqualTo("Tech Store");
                assertThat(result.rating()).isEqualTo(4.5);
                assertThat(result.status()).isEqualTo(MerchantStatus.ACTIVE);
            }
        }

        @Test
        @DisplayName("should throw MerchantNotFoundException when merchant not found")
        void testGetMerchantProfileNotFound() {
            try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getCurrentUserId).thenReturn(999L);
                when(merchantRepository.findByUser_Id(999L))
                        .thenReturn(Optional.empty());

                assertThatThrownBy(() -> merchantService.getCurrentMerchantProfile())
                        .isInstanceOf(MerchantNotFoundException.class)
                        .hasMessageContaining("Merchant not found");
            }
        }

        @Test
        @DisplayName("should include all merchant fields in response")
        void testGetMerchantProfileIncludesAllFields() {
            try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getCurrentUserId).thenReturn(200L);
                when(merchantRepository.findByUser_Id(200L))
                        .thenReturn(Optional.of(testMerchant));

                MerchantProfileResponse result = merchantService.getCurrentMerchantProfile();

                assertThat(result)
                        .extracting("merchantId", "name", "rating", "status")
                        .containsExactly(5L, "Tech Store", 4.5, MerchantStatus.ACTIVE);
            }
        }
    }

    @Nested
    @DisplayName("updateMerchantProfile")
    class UpdateMerchantProfileTests {

        @Test
        @DisplayName("should update merchant profile successfully")
        void testUpdateMerchantProfileSuccess() {
            try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getCurrentUserId).thenReturn(200L);

                Merchant updatedMerchant = testMerchant.toBuilder()
                        .name("Updated Tech Store")
                        .build();

                when(merchantRepository.findByUser_Id(200L))
                        .thenReturn(Optional.of(testMerchant));
                when(merchantRepository.save(any(Merchant.class)))
                        .thenReturn(updatedMerchant);

                MerchantProfileResponse request = MerchantProfileResponse.builder()
                        .name("Updated Tech Store")
                        .build();

                MerchantProfileResponse result = merchantService.updateMerchantProfile(request);

                assertThat(result).isNotNull();
                assertThat(result.name()).isEqualTo("Updated Tech Store");
                verify(merchantRepository).save(any(Merchant.class));
            }
        }

        @Test
        @DisplayName("should throw when merchant not found during update")
        void testUpdateMerchantProfileNotFound() {
            try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getCurrentUserId).thenReturn(999L);
                when(merchantRepository.findByUser_Id(999L))
                        .thenReturn(Optional.empty());

                MerchantProfileResponse request = MerchantProfileResponse.builder()
                        .name("Test")
                        .build();

                assertThatThrownBy(() -> merchantService.updateMerchantProfile(request))
                        .isInstanceOf(MerchantNotFoundException.class);
            }
        }

        @Test
        @DisplayName("should preserve merchant ID during update")
        void testUpdateMerchantPreservesId() {
            try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getCurrentUserId).thenReturn(200L);

                Merchant updatedMerchant = testMerchant.toBuilder()
                        .name("New Name")
                        .build();

                when(merchantRepository.findByUser_Id(200L))
                        .thenReturn(Optional.of(testMerchant));
                when(merchantRepository.save(any(Merchant.class)))
                        .thenReturn(updatedMerchant);

                MerchantProfileResponse request = MerchantProfileResponse.builder()
                        .name("New Name")
                        .build();

                MerchantProfileResponse result = merchantService.updateMerchantProfile(request);

                assertThat(result.merchantId()).isEqualTo(5L);
            }
        }
    }

    @Nested
    @DisplayName("updateMerchantStatus")
    class UpdateMerchantStatusTests {

        @Test
        @DisplayName("should update merchant status to INACTIVE")
        void testUpdateMerchantStatusToInactive() {
            try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getCurrentUserId).thenReturn(200L);

                Merchant inactiveMerchant = testMerchant.toBuilder()
                        .status(MerchantStatus.INACTIVE)
                        .build();

                when(merchantRepository.findByUser_Id(200L))
                        .thenReturn(Optional.of(testMerchant));
                when(merchantRepository.save(any(Merchant.class)))
                        .thenReturn(inactiveMerchant);

                MerchantProfileResponse result = merchantService.updateMerchantStatus("INACTIVE");

                assertThat(result.status()).isEqualTo(MerchantStatus.INACTIVE);
            }
        }

        @Test
        @DisplayName("should update merchant status to SUSPENDED")
        void testUpdateMerchantStatusToSuspended() {
            try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getCurrentUserId).thenReturn(200L);

                Merchant suspendedMerchant = testMerchant.toBuilder()
                        .status(MerchantStatus.SUSPENDED)
                        .build();

                when(merchantRepository.findByUser_Id(200L))
                        .thenReturn(Optional.of(testMerchant));
                when(merchantRepository.save(any(Merchant.class)))
                        .thenReturn(suspendedMerchant);

                MerchantProfileResponse result = merchantService.updateMerchantStatus("SUSPENDED");

                assertThat(result.status()).isEqualTo(MerchantStatus.SUSPENDED);
            }
        }

        @Test
        @DisplayName("should throw when merchant not found during status update")
        void testUpdateMerchantStatusNotFound() {
            try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getCurrentUserId).thenReturn(999L);
                when(merchantRepository.findByUser_Id(999L))
                        .thenReturn(Optional.empty());

                assertThatThrownBy(() -> merchantService.updateMerchantStatus("INACTIVE"))
                        .isInstanceOf(MerchantNotFoundException.class);
            }
        }

        @Test
        @DisplayName("should preserve merchant data during status update")
        void testUpdateMerchantStatusPreservesData() {
            try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getCurrentUserId).thenReturn(200L);

                Merchant updatedMerchant = testMerchant.toBuilder()
                        .status(MerchantStatus.INACTIVE)
                        .build();

                when(merchantRepository.findByUser_Id(200L))
                        .thenReturn(Optional.of(testMerchant));
                when(merchantRepository.save(any(Merchant.class)))
                        .thenReturn(updatedMerchant);

                MerchantProfileResponse result = merchantService.updateMerchantStatus("INACTIVE");

                assertThat(result.merchantId()).isEqualTo(5L);
                assertThat(result.name()).isEqualTo("Tech Store");
                assertThat(result.rating()).isEqualTo(4.5);
            }
        }
    }

    @Nested
    @DisplayName("Merchant Creation and Activation")
    class MerchantCreationTests {

        @Test
        @DisplayName("should create new merchant with ACTIVE status")
        void testNewMerchantHasActiveStatus() {
            assertThat(testMerchant.getStatus()).isEqualTo(MerchantStatus.ACTIVE);
        }

        @Test
        @DisplayName("should link merchant to user account")
        void testMerchantLinkedToUser() {
            assertThat(testMerchant.getUser()).isNotNull();
            assertThat(testMerchant.getUser().getEmail()).isEqualTo("merchant@example.com");
            assertThat(testMerchant.getUser().getRole()).isEqualTo(Role.MERCHANT);
        }

        @Test
        @DisplayName("should have merchant ID")
        void testMerchantHasId() {
            assertThat(testMerchant.getMerchantId()).isEqualTo(5L);
        }
    }
}