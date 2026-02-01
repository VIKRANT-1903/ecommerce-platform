package com.example.ecomm.checkout.service;

import java.math.BigDecimal;

/**
 * Dummy payment gateway for checkout orchestration.
 * Returns random success/failure for testing.
 */
public interface PaymentGateway {

    /**
     * Process payment for an order. Dummy implementation returns random success/failure.
     *
     * @param orderId order identifier
     * @param amount  total amount to charge
     * @return true if payment succeeded, false otherwise
     */
    boolean processPayment(Long orderId, BigDecimal amount);
}
