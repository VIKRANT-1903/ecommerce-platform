package com.example.ecomm.common.exception;

import lombok.experimental.StandardException;

/**
 * Thrown when a payment or payment-related operation fails.
 */
@StandardException
public class PaymentFailedException extends RuntimeException {
}
