//package com.example.ecomm.email.service;
//
//import com.example.ecomm.user.repository.UserRepository;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.scheduling.annotation.Async;
//import org.springframework.stereotype.Service;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class EmailService {
//
//    private final UserRepository userRepository;
//
//    /**
//     * Send order confirmation or failure notification to the user's email (from users table).
//     * Runs asynchronously. Dummy implementation: logs only. Failures are logged but do not affect checkout.
//     */
//    @Async
//    public void sendOrderConfirmation(Long orderId, Integer userId, boolean success) {
//        try {
//            String email = userRepository.findById(userId.longValue())
//                    .map(user -> user.getEmail())
//                    .orElse(null);
//
//            if (email == null) {
//                log.warn("User {} not found, skipping email notification for order {}", userId, orderId);
//                return;
//            }
//
//            if (success) {
//                log.info("[EMAIL] Order confirmation sent to {}: orderId={}, userId={}", email, orderId, userId);
//            } else {
//                log.info("[EMAIL] Order failure notification sent to {}: orderId={}, userId={}", email, orderId, userId);
//            }
//        } catch (Exception e) {
//            log.error("Email notification failed for order {} user {}: {}", orderId, userId, e.getMessage(), e);
//        }
//    }
//}
package com.example.ecomm.email.service;

import com.example.ecomm.user.entity.User;
import com.example.ecomm.user.repository.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final UserRepository userRepository;

    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * Sends an email asynchronously.
     * Exceptions are caught so the checkout process doesn't fail if the email server is down.
     */
    @Async
    public void sendOrderConfirmation(Long orderId, Integer userId, boolean success) {
        try {
            // 1. Fetch User Email
            String toEmail = userRepository.findById(userId.longValue())
                    .map(User::getEmail)
                    .orElse(null);

            if (toEmail == null) {
                log.warn("User {} has no email found. Skipping notification.", userId);
                return;
            }

            // 2. Prepare the Email
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);

            if (success) {
                helper.setSubject("Order Confirmation - Order #" + orderId);
                helper.setText(buildSuccessEmailBody(orderId, userId), true); // true = HTML
            } else {
                helper.setSubject("Order Failed - Order #" + orderId);
                helper.setText(buildFailureEmailBody(orderId), true);
            }

            // 3. Send
            mailSender.send(message);
            log.info("Email sent successfully to {}", toEmail);

        } catch (MessagingException | RuntimeException e) {
            log.error("Failed to send email for Order {}: {}", orderId, e.getMessage());
        }
    }

    private String buildSuccessEmailBody(Long orderId, Integer userId) {
        return "<html>" +
                "<body>" +
                "<h1>Order Confirmed!</h1>" +
                "<p>Hi " + userId + ",</p>" +
                "<p>Thank you for your purchase. Your order <b>#" + orderId + "</b> has been successfully placed.</p>" +
                "<p>We will notify you when it ships!</p>" +
                "<br/>" +
                "<p>Best Regards,<br/>E-Comm Team</p>" +
                "</body>" +
                "</html>";
    }

    private String buildFailureEmailBody(Long orderId) {
        return "<html>" +
                "<body>" +
                "<h1>Order Failed</h1>" +
                "<p>We encountered an issue processing your order <b>#" + orderId + "</b>.</p>" +
                "<p>You have NOT been charged. Please try checking out again.</p>" +
                "<br/>" +
                "<p>Best Regards,<br/>E-Comm Team</p>" +
                "</body>" +
                "</html>";
    }
}
