package com.bookmyshow.payment;

public class CreditCardPayment implements PaymentStrategy {
    private final String cardNumber;

    public CreditCardPayment(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    @Override
    public boolean pay(double amount) {
        String masked = "****" + cardNumber.substring(Math.max(0, cardNumber.length() - 4));
        System.out.println("    [PAYMENT] Charged Rs." + amount + " to card " + masked);
        return true;
    }

    @Override
    public PaymentType getType() {
        return PaymentType.CREDIT_CARD;
    }
}
