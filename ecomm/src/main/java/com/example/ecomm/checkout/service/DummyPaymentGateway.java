package com.example.ecomm.checkout.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Random;

@Component
@Slf4j
public class DummyPaymentGateway implements PaymentGateway {

    private final Random random = new Random();

    @Override
    public boolean processPayment(Long orderId, BigDecimal amount) {
        boolean success = true;
        log.info("Dummy payment for order {} amount {}: {}", orderId, amount, success ? "SUCCESS" : "FAILED");
        return success;
    }
}
