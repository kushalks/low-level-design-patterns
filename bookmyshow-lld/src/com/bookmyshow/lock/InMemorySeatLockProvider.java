package com.bookmyshow.lock;

import com.bookmyshow.model.Show;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory seat lock implementation - correct within a single JVM.
 *
 * ===================== THE CONCURRENCY CORE =====================
 * The race: two users click the SAME seat at the SAME time. We must let exactly
 * one through. The danger is the classic read-then-write gap:
 *     read "is it locked?"  ->  (other thread sneaks in)  ->  write "I locked it"
 *
 * Fix: ConcurrentHashMap.compute() runs our lambda while holding the lock on that
 * ONE key's bin. So "check existing lock + decide + set new lock" is a single
 * atomic, uninterruptible step per seat. Different seats don't block each other.
 *
 * acquireOne() is the atomic primitive; lockSeats() composes it over many seats
 * with all-or-nothing rollback.
 * ===============================================================
 */
public class InMemorySeatLockProvider implements SeatLockProvider {

    // key = showId + ":" + seatId  ->  current lock (if any)
    private final Map<String, SeatLock> locks = new ConcurrentHashMap<>();
    private final long lockTtlMillis;

    public InMemorySeatLockProvider(long lockTtlMillis) {
        this.lockTtlMillis = lockTtlMillis;
    }

    @Override
    public boolean lockSeats(Show show, List<String> seatIds, String userId) {
        List<String> acquired = new ArrayList<>();
        for (String seatId : seatIds) {
            if (acquireOne(show.getId(), seatId, userId)) {
                acquired.add(seatId);
            } else {
                // Partial failure: someone else holds one of our seats. Roll back
                // everything we grabbed so we don't strand seats.
                for (String s : acquired) {
                    releaseOne(show.getId(), s, userId);
                }
                return false;
            }
        }
        return true;
    }

    /** The atomic check-and-set for a single seat. */
    private boolean acquireOne(String showId, String seatId, String userId) {
        String key = key(showId, seatId);
        boolean[] success = {false};
        locks.compute(key, (k, existing) -> {
            // Free to take if: never locked, OR previous lock expired, OR it's
            // the same user re-locking (re-entrant / idempotent retry).
            if (existing == null || existing.isExpired() || existing.getUserId().equals(userId)) {
                success[0] = true;
                return new SeatLock(userId, Instant.now().plusMillis(lockTtlMillis));
            }
            // Held by a different, still-valid user -> keep theirs, we fail.
            return existing;
        });
        return success[0];
    }

    @Override
    public void unlockSeats(Show show, List<String> seatIds, String userId) {
        for (String seatId : seatIds) {
            releaseOne(show.getId(), seatId, userId);
        }
    }

    private void releaseOne(String showId, String seatId, String userId) {
        String key = key(showId, seatId);
        // Only remove if WE still hold it - never yank someone else's lock.
        locks.computeIfPresent(key, (k, existing) ->
                existing.getUserId().equals(userId) ? null : existing);
    }

    @Override
    public boolean isLocked(Show show, String seatId) {
        SeatLock lock = locks.get(key(show.getId(), seatId));
        return lock != null && !lock.isExpired();
    }

    private String key(String showId, String seatId) {
        return showId + ":" + seatId;
    }
}
