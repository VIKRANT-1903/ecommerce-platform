package com.example.ecomm.cart.entity;

/**
 * Cart lifecycle status.
 */
public enum CartStatus {
    ACTIVE,     // Cart is active and can be modified
    CHECKED_OUT // Cart has been checked out and converted to an order
  // Cart has been abandoned (e.g. after a timeout)
}
