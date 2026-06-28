package com.bookmyshow.model;

/**
 * Permanent availability of a seat for a specific show.
 *
 * Note the distinction from a *lock*: a lock is a short-lived, in-flight hold
 * while a user is paying (managed by SeatLockProvider). BOOKED here is the final,
 * durable state after payment succeeds.
 */
public enum SeatStatus {
    AVAILABLE,
    BOOKED
}
