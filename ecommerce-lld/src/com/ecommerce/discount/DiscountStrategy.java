package com.ecommerce.discount;

/**
 * STRATEGY PATTERN for pricing/discounts.
 *
 * Separating discount logic from the cart/order lets us plug in coupon codes,
 * seasonal sales, loyalty discounts, etc. without touching checkout code.
 */
public interface DiscountStrategy {
    /**
     * @param subtotal the pre-discount amount
     * @return the amount payable after applying this discount
     */
    double apply(double subtotal);

    String describe();
}
