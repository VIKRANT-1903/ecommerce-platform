package com.example.ecomm.email.service;

import com.example.ecomm.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final UserRepository userRepository;

    /**
     * Send order confirmation or failure notification to the user's email (from users table).
     * Runs asynchronously. Dummy implementation: logs only. Failures are logged but do not affect checkout.
     */
    @Async
    public void sendOrderConfirmation(Long orderId, Integer userId, boolean success) {
        try {
            String email = userRepository.findById(userId.longValue())
                    .map(user -> user.getEmail())
                    .orElse(null);

            if (email == null) {
                log.warn("User {} not found, skipping email notification for order {}", userId, orderId);
                return;
            }

            if (success) {
                log.info("[EMAIL] Order confirmation sent to {}: orderId={}, userId={}", email, orderId, userId);
            } else {
                log.info("[EMAIL] Order failure notification sent to {}: orderId={}, userId={}", email, orderId, userId);
            }
        } catch (Exception e) {
            log.error("Email notification failed for order {} user {}: {}", orderId, userId, e.getMessage(), e);
        }
    }
}
