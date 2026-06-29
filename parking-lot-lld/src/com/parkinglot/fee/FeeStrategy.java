package com.parkinglot.fee;

import com.parkinglot.model.Ticket;

import java.time.Instant;

/**
 * STRATEGY PATTERN - how a parking fee is computed.
 *
 * Lets us swap flat-rate, hourly, or weekend pricing without touching the lot.
 */
public interface FeeStrategy {
    double calculate(Ticket ticket, Instant exitTime);
}
