package com.example.ecomm.common.exception;

import lombok.experimental.StandardException;

/**
 * Thrown when a distributed or local lock cannot be acquired.
 */
@StandardException
public class LockAcquisitionException extends RuntimeException {
}
