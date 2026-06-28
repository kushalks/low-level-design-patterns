package com.bookmyshow.payment;

/** FACTORY PATTERN - centralises creation of payment strategies. */
public class PaymentFactory {

    public static PaymentStrategy create(PaymentType type, String detail) {
        switch (type) {
            case CREDIT_CARD: return new CreditCardPayment(detail);
            case UPI:         return new UpiPayment(detail);
            case WALLET:      return new UpiPayment(detail); // simplified for demo
            default: throw new IllegalArgumentException("Unsupported payment type: " + type);
        }
    }
}
