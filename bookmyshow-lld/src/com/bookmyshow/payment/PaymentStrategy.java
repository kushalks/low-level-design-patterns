package com.bookmyshow.payment;

/** STRATEGY PATTERN - interchangeable payment methods. */
public interface PaymentStrategy {
    boolean pay(double amount);
    PaymentType getType();
}
