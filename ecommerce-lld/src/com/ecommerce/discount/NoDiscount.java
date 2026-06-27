package com.ecommerce.discount;

/**
 * Null Object - represents "no discount". Lets callers avoid null checks by
 * always having a strategy to call.
 */
public class NoDiscount implements DiscountStrategy {
    @Override
    public double apply(double subtotal) {
        return subtotal;
    }

    @Override
    public String describe() {
        return "No discount";
    }
}
