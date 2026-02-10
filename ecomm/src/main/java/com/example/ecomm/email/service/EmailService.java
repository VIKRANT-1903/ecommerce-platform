package com.example.ecomm.email.service;

import com.example.ecomm.order.entity.Order; // Can keep import if needed for fetching
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
     * Reverted to ID-based signature to fix CheckoutService compilation errors.
     */
    @Async
    public void sendOrderConfirmation(Long orderId, Integer userId, boolean success) {
        try {
            String toEmail = userRepository.findById(userId.longValue())
                    .map(User::getEmail)
                    .orElse(null);

            if (toEmail == null) {
                log.warn("User {} has no email found. Skipping notification.", userId);
                return;
            }

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);

            if (success) {
                helper.setSubject("Order Confirmation - Order #" + orderId);
                helper.setText(buildSuccessEmailBody(orderId, userId), true);
            } else {
                helper.setSubject("Order Failed - Order #" + orderId);
                helper.setText(buildFailureEmailBody(orderId), true);
            }

            mailSender.send(message);
            log.info("Email notification sent to {}", toEmail);

        } catch (MessagingException e) {
            log.error("Failed to send email for Order {}", orderId, e);
        }
    }

    private String buildSuccessEmailBody(Long orderId, Integer userId) {
        return "<html>" +
                "<body>" +
                "<h1>Order Confirmed!</h1>" +
                "<p>Hi Sir or Mam,</p>" + // Kept your requested greeting
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