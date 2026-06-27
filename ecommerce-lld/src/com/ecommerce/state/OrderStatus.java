package com.ecommerce.state;

/**
 * The discrete statuses an order can be in. Used as a lightweight label;
 * the actual transition rules live in the OrderState implementations.
 */
public enum OrderStatus {
    CREATED,
    PAID,
    SHIPPED,
    DELIVERED,
    CANCELLED
}
