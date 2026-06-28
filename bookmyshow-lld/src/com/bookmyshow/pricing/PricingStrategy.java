package com.bookmyshow.pricing;

import com.bookmyshow.model.SeatType;

/**
 * STRATEGY PATTERN - computes the price of a seat for a show.
 *
 * Pulling pricing behind an interface lets us add weekend surcharges, dynamic
 * (demand-based) pricing, or promo pricing without touching show setup.
 */
public interface PricingStrategy {
    double priceFor(SeatType seatType);
}
