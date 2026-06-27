package com.ecommerce.discount;

/** Takes a percentage off the subtotal, optionally capped at a maximum amount. */
public class PercentageDiscount implements DiscountStrategy {
    private final double percent;
    private final double maxDiscount;

    public PercentageDiscount(double percent, double maxDiscount) {
        if (percent < 0 || percent > 100) {
            throw new IllegalArgumentException("Percent must be between 0 and 100");
        }
        this.percent = percent;
        this.maxDiscount = maxDiscount;
    }

    @Override
    public double apply(double subtotal) {
        double discount = subtotal * (percent / 100.0);
        discount = Math.min(discount, maxDiscount);
        return subtotal - discount;
    }

    @Override
    public String describe() {
        return percent + "% off (max $" + maxDiscount + ")";
    }
}
