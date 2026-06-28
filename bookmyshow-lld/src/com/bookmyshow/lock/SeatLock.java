package com.bookmyshow.lock;

import java.time.Instant;

/**
 * A short-lived hold on a seat while a user completes payment.
 *
 * The expiry is what makes the system self-healing: if a user locks seats and
 * abandons the flow (closes the tab, payment hangs), the lock simply expires and
 * the seat frees up - no manual cleanup, no seat stuck forever.
 */
public class SeatLock {
    private final String userId;
    private final Instant expiresAt;

    public SeatLock(String userId, Instant expiresAt) {
        this.userId = userId;
        this.expiresAt = expiresAt;
    }

    public String getUserId() { return userId; }
    public Instant getExpiresAt() { return expiresAt; }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public boolean isHeldBy(String userId) {
        return this.userId.equals(userId) && !isExpired();
    }
}
