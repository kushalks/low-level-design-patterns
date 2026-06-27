package com.ecommerce.payment;

public class CreditCardPayment implements PaymentStrategy {
    private final String cardNumber;

    public CreditCardPayment(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    @Override
    public boolean pay(double amount) {
        // In reality: call a payment gateway (Stripe/Razorpay), handle 3-D Secure, etc.
        String masked = "****" + cardNumber.substring(Math.max(0, cardNumber.length() - 4));
        System.out.println("  [PAYMENT] Charged $" + amount + " to credit card " + masked);
        return true;
    }

    @Override
    public PaymentType getType() {
        return PaymentType.CREDIT_CARD;
    }
}
