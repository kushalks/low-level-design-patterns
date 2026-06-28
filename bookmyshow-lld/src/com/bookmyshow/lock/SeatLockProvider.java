package com.bookmyshow.lock;

import com.bookmyshow.model.Show;

import java.util.List;

/**
 * STRATEGY PATTERN for concurrency control.
 *
 * This is the single most important abstraction in the whole design. It hides
 * *how* seats are held during checkout. Swapping the implementation changes the
 * scaling story without touching booking logic:
 *
 *   - InMemorySeatLockProvider : single JVM, ConcurrentHashMap (this repo).
 *   - (described) RedisSeatLockProvider : SET key val NX PX ttl across servers.
 *   - (described) DbSeatLockProvider : SELECT ... FOR UPDATE / version column.
 *
 * The contract: lockSeats is ALL-OR-NOTHING and atomic per seat. If any requested
 * seat can't be locked, none are locked.
 */
public interface SeatLockProvider {

    /**
     * Atomically acquire short-lived locks on all the given seats for one user.
     * @return true only if EVERY seat was locked; on partial failure it rolls back.
     */
    boolean lockSeats(Show show, List<String> seatIds, String userId);

    /** Release locks held by this user (e.g. after payment success or failure). */
    void unlockSeats(Show show, List<String> seatIds, String userId);

    /** True if the seat is currently locked by someone (and the lock hasn't expired). */
    boolean isLocked(Show show, String seatId);
}
