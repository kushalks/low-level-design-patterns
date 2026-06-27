package com.ecommerce.payment;

public class WalletPayment implements PaymentStrategy {
    private double balance;

    public WalletPayment(double balance) {
        this.balance = balance;
    }

    @Override
    public boolean pay(double amount) {
        if (amount > balance) {
            System.out.println("  [PAYMENT] Wallet declined: insufficient balance ($"
                    + balance + " < $" + amount + ")");
            return false;
        }
        balance -= amount;
        System.out.println("  [PAYMENT] Paid $" + amount + " from wallet. Remaining: $" + balance);
        return true;
    }

    @Override
    public PaymentType getType() {
        return PaymentType.WALLET;
    }
}
