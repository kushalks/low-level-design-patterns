package com.bookmyshow.service;

import com.bookmyshow.lock.SeatLockProvider;
import com.bookmyshow.model.Booking;
import com.bookmyshow.model.BookingStatus;
import com.bookmyshow.model.SeatStatus;
import com.bookmyshow.model.Show;
import com.bookmyshow.model.ShowSeat;
import com.bookmyshow.model.User;
import com.bookmyshow.notification.BookingObserver;
import com.bookmyshow.payment.PaymentStrategy;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Orchestrates the booking workflow. This is where the seat-lock lifecycle and
 * payment come together. Dependencies are injected (Dependency Inversion) so the
 * lock provider can be in-memory now and Redis/DB later with zero changes here.
 *
 * THE FLOW (and why each step exists):
 *   1. Validate the requested seats are still AVAILABLE (fast pre-check).
 *   2. lockSeats() - the ATOMIC gate. Only one concurrent user wins a seat.
 *   3. Create the booking (CREATED) and compute the amount.
 *   4. Take payment.
 *      - success -> mark seats BOOKED, release locks, CONFIRMED.
 *      - failure -> release locks, EXPIRED. Seats free up immediately.
 *
 * Note: locks are transient holds; the BOOKED ShowSeat status is the durable
 * truth. We only set BOOKED *after* payment, while still holding the lock - so no
 * one else can have grabbed the seat in between.
 */
public class BookingService {
    private final SeatLockProvider seatLockProvider;
    private final List<BookingObserver> observers;
    private final Map<String, Booking> bookings = new ConcurrentHashMap<>();

    public BookingService(SeatLockProvider seatLockProvider, List<BookingObserver> observers) {
        this.seatLockProvider = seatLockProvider;
        this.observers = observers;
    }

    public Booking book(User user, Show show, List<String> seatIds, PaymentStrategy payment) {
        // 1. Pre-check availability (cheap; the real guard is the lock in step 2).
        for (String seatId : seatIds) {
            ShowSeat showSeat = show.getShowSeat(seatId);
            if (showSeat == null) {
                throw new IllegalArgumentException("No such seat: " + seatId);
            }
            if (showSeat.getStatus() != SeatStatus.AVAILABLE) {
                throw new SeatUnavailableException("Seat already booked: " + seatId);
            }
        }

        // 2. ATOMIC, all-or-nothing lock. This is the concurrency gate.
        boolean locked = seatLockProvider.lockSeats(show, seatIds, user.getId());
        if (!locked) {
            throw new SeatUnavailableException(
                    "Could not lock seats " + seatIds + " - taken by another user");
        }

        // From here we hold the locks, so we can work safely.
        try {
            // 3. RE-CHECK under the lock (double-checked locking). The pre-check in
            // step 1 is racy: a seat could have been confirmed BOOKED between that
            // read and us acquiring the lock. Now that we hold the lock, this read
            // is authoritative - if it's already booked, bail out.
            for (String seatId : seatIds) {
                if (show.getShowSeat(seatId).getStatus() != SeatStatus.AVAILABLE) {
                    throw new SeatUnavailableException("Seat just got booked: " + seatId);
                }
            }

            double amount = seatIds.stream()
                    .mapToDouble(id -> show.getShowSeat(id).getPrice())
                    .sum();

            Booking booking = new Booking(
                    "BKG-" + UUID.randomUUID().toString().substring(0, 8),
                    user, show, seatIds, amount);
            bookings.put(booking.getId(), booking);

            // 4. Payment.
            boolean paid = payment.pay(amount);
            if (!paid) {
                booking.setStatus(BookingStatus.EXPIRED);
                notifyObservers(booking);
                return booking;
            }

            // Payment OK: commit seats to BOOKED while still holding the lock.
            for (String seatId : seatIds) {
                show.getShowSeat(seatId).setStatus(SeatStatus.BOOKED);
            }
            booking.setStatus(BookingStatus.CONFIRMED);
            notifyObservers(booking);
            return booking;

        } finally {
            // 5. Always release the transient locks - BOOKED status now protects
            // confirmed seats; failed bookings free the seats for others.
            seatLockProvider.unlockSeats(show, seatIds, user.getId());
        }
    }

    public Booking getBooking(String bookingId) {
        return bookings.get(bookingId);
    }

    private void notifyObservers(Booking booking) {
        for (BookingObserver observer : observers) {
            observer.onBookingUpdated(booking);
        }
    }
}
