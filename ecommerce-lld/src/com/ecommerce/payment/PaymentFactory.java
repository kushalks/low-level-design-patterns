package com.ecommerce.payment;

/**
 * FACTORY PATTERN.
 *
 * Centralises creation of payment strategies so callers don't depend on concrete
 * classes. The "details" map would, in a real system, carry card tokens / VPA /
 * wallet ids pulled from a secure source.
 */
public class PaymentFactory {

    public static PaymentStrategy create(PaymentType type, String detail) {
        switch (type) {
            case CREDIT_CARD:
                return new CreditCardPayment(detail);
            case UPI:
                return new UpiPayment(detail);
            case WALLET:
                // detail interpreted as the wallet balance here for the demo.
                return new WalletPayment(Double.parseDouble(detail));
            default:
                throw new IllegalArgumentException("Unsupported payment type: " + type);
        }
    }
}
