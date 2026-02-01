package com.example.ecomm.common.exception;

import lombok.experimental.StandardException;

/**
 * Thrown when requested quantity exceeds available inventory.
 */
@StandardException
public class InsufficientInventoryException extends RuntimeException {
}
