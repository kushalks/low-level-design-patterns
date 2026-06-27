package com.ecommerce.discount;

/** Flat amount off, applied only above a minimum order value. */
public class FlatDiscount implements DiscountStrategy {
    private final double amount;
    private final double minOrderValue;

    public FlatDiscount(double amount, double minOrderValue) {
        this.amount = amount;
        this.minOrderValue = minOrderValue;
    }

    @Override
    public double apply(double subtotal) {
        if (subtotal < minOrderValue) {
            return subtotal; // condition not met
        }
        return Math.max(0, subtotal - amount);
    }

    @Override
    public String describe() {
        return "$" + amount + " off on orders above $" + minOrderValue;
    }
}
