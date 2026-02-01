package com.example.ecomm.common.exception;

import lombok.experimental.StandardException;

/**
 * Thrown when a requested resource does not exist.
 */
@StandardException
public class ResourceNotFoundException extends RuntimeException {
}
