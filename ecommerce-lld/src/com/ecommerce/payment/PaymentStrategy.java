package com.ecommerce.payment;

/**
 * STRATEGY PATTERN.
 *
 * Each payment method is an interchangeable algorithm. The checkout flow depends
 * only on this abstraction, so adding a new payment method (e.g. NetBanking,
 * BuyNowPayLater) requires no change to the OrderService - just a new strategy.
 */
public interface PaymentStrategy {
    /**
     * Attempt to charge the given amount.
     * @return true if payment succeeded.
     */
    boolean pay(double amount);

    PaymentType getType();
}
