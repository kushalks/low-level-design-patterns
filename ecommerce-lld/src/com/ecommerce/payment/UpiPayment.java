package com.ecommerce.payment;

public class UpiPayment implements PaymentStrategy {
    private final String vpa; // virtual payment address e.g. user@bank

    public UpiPayment(String vpa) {
        this.vpa = vpa;
    }

    @Override
    public boolean pay(double amount) {
        System.out.println("  [PAYMENT] Collected $" + amount + " via UPI from " + vpa);
        return true;
    }

    @Override
    public PaymentType getType() {
        return PaymentType.UPI;
    }
}
