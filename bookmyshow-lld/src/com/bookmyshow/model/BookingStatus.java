package com.bookmyshow.model;

/**
 * Lifecycle of a booking:
 *
 *   CREATED  -> seats locked, awaiting payment
 *   CONFIRMED-> payment succeeded, seats permanently BOOKED
 *   EXPIRED  -> lock timed out or payment failed; seats released
 *   CANCELLED-> user cancelled a confirmed booking; seats released
 */
public enum BookingStatus {
    CREATED,
    CONFIRMED,
    EXPIRED,
    CANCELLED
}
