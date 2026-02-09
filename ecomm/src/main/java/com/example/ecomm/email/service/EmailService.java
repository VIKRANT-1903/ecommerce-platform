package com.example.ecomm.email.service;

import com.example.ecomm.order.entity.Order;
import com.example.ecomm.order.repository.OrderRepository;
import com.example.ecomm.user.entity.User;
import com.example.ecomm.user.repository.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
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
    private final OrderRepository orderRepository;

    @Autowired
    @Lazy
    private EmailService self;

    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * Entry point: Fetches order synchronously to avoid race conditions,
     * calculates friendly number, then sends email async.
     */
    public void sendOrderConfirmation(Long orderId, Integer userId, boolean success) {
        if (!success) {
            self.sendFailureEmail(orderId, userId);
            return;
        }

        // 1. Fetch Order Sync
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            log.error("Cannot send email: Order {} not found", orderId);
            return;
        }

        // 2. Calculate Friendly Number
        long friendlyCount = orderRepository.countByUserIdAndOrderIdLessThanEqual(userId, orderId);
        order.setUserOrderNumber((int) friendlyCount);

        // 3. Send Async
        self.sendOrderConfirmation(order);
    }

    @Async
    public void sendOrderConfirmation(Order order) {
        try {
            String toEmail = userRepository.findById(order.getUserId().longValue())
                    .map(User::getEmail)
                    .orElse(null);

            if (toEmail == null) return;

            // Use Friendly ID (#1) or fallback to Global ID (#57)
            Integer displayId = order.getUserOrderNumber() != null
                    ? order.getUserOrderNumber()
                    : order.getOrderId().intValue();

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Order Confirmation - Order #" + displayId);

            String htmlContent = buildSuccessEmailBody(order, displayId);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Order confirmation email sent to {}", toEmail);

        } catch (MessagingException e) {
            log.error("Failed to send email", e);
        }
    }

    @Async
    public void sendFailureEmail(Long orderId, Integer userId) {
        try {
            String toEmail = userRepository.findById(userId.longValue()).map(User::getEmail).orElse(null);
            if (toEmail == null) return;

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Order Payment Failed - Order #" + orderId);
            helper.setText("Hi Sir or Mam,<br><br>We could not process payment for Order #" + orderId + ". Please try again.", true);

            mailSender.send(message);
        } catch (Exception e) {
            log.error("Failed to send failure email", e);
        }
    }

    // --- SIMPLIFIED EMAIL BODY (No Product List) ---
    private String buildSuccessEmailBody(Order order, Integer displayId) {
        return """
            <html>
            <body style="font-family: Arial, sans-serif; color: #333;">
                <div style="max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #eee; border-radius: 10px;">
                    <h2 style="color: #FF9900;">Order Confirmed!</h2>
                    
                    <p style="font-size: 16px;">Hi Sir or Mam,</p>
                    
                    <p>Thank you for your purchase. Your order <strong>#%d</strong> has been successfully placed.</p>
                    
                    <p>We will notify you when it ships!</p>
                    
                    <div style="margin-top: 20px; padding: 15px; background-color: #f9f9f9; border-radius: 5px;">
                        <p style="margin: 0; font-size: 18px;">
                            <strong>Total Amount: $%s</strong>
                        </p>
                    </div>
                    
                    <br/>
                    <p style="color: #777; font-size: 12px;">Best Regards,<br/>ShopZone Team</p>
                </div>
            </body>
            </html>
            """.formatted(displayId, order.getTotalAmount());
    }
}