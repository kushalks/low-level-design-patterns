package com.bookmyshow.model;

import java.util.Collections;
import java.util.List;

/**
 * A user's attempt to reserve specific seats for a show. Created the moment seats
 * are locked; transitions to CONFIRMED on payment or EXPIRED/CANCELLED otherwise.
 */
public class Booking {
    private final String id;
    private final User user;
    private final Show show;
    private final List<String> seatIds;
    private final double amount;
    private volatile BookingStatus status;

    public Booking(String id, User user, Show show, List<String> seatIds, double amount) {
        this.id = id;
        this.user = user;
        this.show = show;
        this.seatIds = seatIds;
        this.amount = amount;
        this.status = BookingStatus.CREATED;
    }

    public String getId() { return id; }
    public User getUser() { return user; }
    public Show getShow() { return show; }
    public List<String> getSeatIds() { return Collections.unmodifiableList(seatIds); }
    public double getAmount() { return amount; }
    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }

    @Override
    public String toString() {
        return "Booking{" + id + ", user=" + user + ", seats=" + seatIds
                + ", amount=$" + amount + ", status=" + status + "}";
    }
}
