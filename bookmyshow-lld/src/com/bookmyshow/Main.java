package com.bookmyshow;

import com.bookmyshow.lock.InMemorySeatLockProvider;
import com.bookmyshow.lock.SeatLockProvider;
import com.bookmyshow.model.Booking;
import com.bookmyshow.model.City;
import com.bookmyshow.model.Movie;
import com.bookmyshow.model.Screen;
import com.bookmyshow.model.Seat;
import com.bookmyshow.model.SeatType;
import com.bookmyshow.model.Show;
import com.bookmyshow.model.Theatre;
import com.bookmyshow.model.User;
import com.bookmyshow.notification.BookingObserver;
import com.bookmyshow.notification.EmailBookingObserver;
import com.bookmyshow.payment.PaymentFactory;
import com.bookmyshow.payment.PaymentStrategy;
import com.bookmyshow.payment.PaymentType;
import com.bookmyshow.pricing.PricingStrategy;
import com.bookmyshow.pricing.SeatTypePricingStrategy;
import com.bookmyshow.service.BookingService;
import com.bookmyshow.service.SeatUnavailableException;
import com.bookmyshow.service.ShowService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        // ---- Bootstrap (manual dependency injection) ----
        SeatLockProvider lockProvider = new InMemorySeatLockProvider(5 * 60_000); // 5-min hold
        List<BookingObserver> observers = Collections.singletonList(new EmailBookingObserver());
        ShowService showService = new ShowService();
        BookingService bookingService = new BookingService(lockProvider, observers);

        // ---- Seed a theatre with one screen of seats ----
        City city = new City("C1", "Bengaluru");
        Screen screen = new Screen("SCR1", "Audi 1", buildSeats());
        Theatre theatre = new Theatre("T1", "PVR Forum", city, Collections.singletonList(screen));
        Movie movie = new Movie("M1", "Inception", 148, "English");

        PricingStrategy pricing = new SeatTypePricingStrategy(); // weekday pricing
        Show show = showService.createShow(movie, screen, LocalDateTime.now().plusDays(1), pricing);

        System.out.println("Theatre: " + theatre);
        System.out.println("Show: " + show);
        System.out.println("Available seats: " + show.getAvailableSeatIds());

        // ---- Happy path: a normal booking ----
        System.out.println("\n=== Normal booking ===");
        User alice = new User("U1", "Alice", "alice@example.com", "+91-90000-00001");
        PaymentStrategy upi = PaymentFactory.create(PaymentType.UPI, "alice@upi");
        Booking aliceBooking = bookingService.book(alice, show, Arrays.asList("A1", "A2"), upi);
        System.out.println("  " + aliceBooking);
        System.out.println("  Available now: " + show.getAvailableSeatIds());

        // ---- THE CONCURRENCY TEST: 10 users race for the SAME seat C5 ----
        System.out.println("\n=== Concurrency: 10 users race for seat C5 ===");
        runSeatRace(bookingService, show, "C5", 10);

        System.out.println("\n  C5 available after race? "
                + show.getAvailableSeatIds().contains("C5") + " (false = it got booked exactly once)");
    }

    /**
     * Fire N threads simultaneously, all trying to book the same seat. Exactly one
     * must succeed; the rest must be cleanly rejected. A CountDownLatch makes them
     * start at the same instant to maximise contention.
     */
    private static void runSeatRace(BookingService bookingService, Show show,
                                    String seatId, int numUsers) throws InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(numUsers);
        CountDownLatch startGun = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(numUsers);
        AtomicInteger successes = new AtomicInteger();
        AtomicInteger failures = new AtomicInteger();

        for (int i = 0; i < numUsers; i++) {
            final int idx = i;
            pool.submit(() -> {
                User user = new User("RU" + idx, "Racer" + idx,
                        "racer" + idx + "@example.com", "+91-90000-1" + idx);
                PaymentStrategy pay = PaymentFactory.create(PaymentType.UPI, "racer" + idx + "@upi");
                try {
                    startGun.await(); // all threads block here, then released together
                    Booking b = bookingService.book(user, show,
                            new ArrayList<>(Collections.singletonList(seatId)), pay);
                    if (b.getStatus().name().equals("CONFIRMED")) {
                        successes.incrementAndGet();
                        System.out.println("  WON  -> " + user.getName() + " booked " + seatId);
                    }
                } catch (SeatUnavailableException e) {
                    failures.incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
        }

        startGun.countDown(); // GO!
        done.await();
        pool.shutdown();

        System.out.println("  Result: " + successes.get() + " success, "
                + failures.get() + " rejected (expected: 1 success)");
    }

    /** Build a small 3-row layout: A=RECLINER, B=PREMIUM, C=REGULAR, 5 seats each. */
    private static List<Seat> buildSeats() {
        List<Seat> seats = new ArrayList<>();
        addRow(seats, "A", SeatType.RECLINER);
        addRow(seats, "B", SeatType.PREMIUM);
        addRow(seats, "C", SeatType.REGULAR);
        return seats;
    }

    private static void addRow(List<Seat> seats, String row, SeatType type) {
        for (int n = 1; n <= 5; n++) {
            seats.add(new Seat(row + n, row, n, type));
        }
    }
}
