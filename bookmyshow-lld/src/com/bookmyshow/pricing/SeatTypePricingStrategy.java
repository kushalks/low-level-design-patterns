package com.bookmyshow.pricing;

import com.bookmyshow.model.SeatType;

/** Simple base pricing by seat type, with an optional multiplier (e.g. weekend). */
public class SeatTypePricingStrategy implements PricingStrategy {
    private final double multiplier;

    public SeatTypePricingStrategy() {
        this(1.0);
    }

    public SeatTypePricingStrategy(double multiplier) {
        this.multiplier = multiplier;
    }

    @Override
    public double priceFor(SeatType seatType) {
        double base;
        switch (seatType) {
            case REGULAR:  base = 200; break;
            case PREMIUM:  base = 350; break;
            case RECLINER: base = 500; break;
            default:       base = 200;
        }
        return base * multiplier;
    }
}
