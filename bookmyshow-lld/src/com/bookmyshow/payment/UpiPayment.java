package com.bookmyshow.payment;

public class UpiPayment implements PaymentStrategy {
    private final String vpa;

    public UpiPayment(String vpa) {
        this.vpa = vpa;
    }

    @Override
    public boolean pay(double amount) {
        System.out.println("    [PAYMENT] Collected Rs." + amount + " via UPI from " + vpa);
        return true;
    }

    @Override
    public PaymentType getType() {
        return PaymentType.UPI;
    }
}
